package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy accessorial drafts by code. */
interface LegacyAccessorialDraftStore {
    fun find(code: String): LegacyAccessorialDraft?
    fun save(item: LegacyAccessorialDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAccessorialDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAccessorialDraftStore(
    var items: MutableMap<String, LegacyAccessorialDraft>,
    var order: MutableList<String>,
) : LegacyAccessorialDraftStore {
    override fun find(code: String): LegacyAccessorialDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAccessorialDraft) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LegacyAccessorialDraft> {
        val out = mutableListOf<LegacyAccessorialDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAccessorialDraftStore(): LegacyAccessorialDraftStore {
    return MemoryLegacyAccessorialDraftStore(
        mutableMapOf<String, LegacyAccessorialDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAccessorialDraftStore(
    var inner: LegacyAccessorialDraftStore,
    var cache: TtlCache<LegacyAccessorialDraft>,
    var hits: Int,
    var misses: Int,
) : LegacyAccessorialDraftStore {
    override fun find(code: String): LegacyAccessorialDraft? {
        val hit = (cache.get(code) as LegacyAccessorialDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAccessorialDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAccessorialDraft> = inner.list()
}

fun newCachedLegacyAccessorialDraftStore(inner: LegacyAccessorialDraftStore, ttl: Int, clock: Clock): LegacyAccessorialDraftStore {
    return CachedLegacyAccessorialDraftStore(
        inner = inner,
        cache = newTtlCache<LegacyAccessorialDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy accessorial drafts. Empty fields match all. */
data class LegacyAccessorialDraftQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAccessorialDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAccessorialDraft>): MutableList<LegacyAccessorialDraft> {
        val matched = filter(items) { candidate: LegacyAccessorialDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAccessorialDrafts(store: LegacyAccessorialDraftStore, query: LegacyAccessorialDraftQuery): MutableList<LegacyAccessorialDraft> {
    return query.apply(store.list())
}
