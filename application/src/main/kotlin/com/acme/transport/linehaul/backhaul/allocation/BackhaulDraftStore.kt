package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul drafts by code. */
interface BackhaulDraftStore {
    fun find(code: String): BackhaulDraft?
    fun save(item: BackhaulDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulDraftStore(
    var items: MutableMap<String, BackhaulDraft>,
    var order: MutableList<String>,
) : BackhaulDraftStore {
    override fun find(code: String): BackhaulDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulDraft) {
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

    override fun list(): MutableList<BackhaulDraft> {
        val out = mutableListOf<BackhaulDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulDraftStore(): BackhaulDraftStore {
    return MemoryBackhaulDraftStore(mutableMapOf<String, BackhaulDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulDraftStore(
    var inner: BackhaulDraftStore,
    var cache: TtlCache<BackhaulDraft>,
    var hits: Int,
    var misses: Int,
) : BackhaulDraftStore {
    override fun find(code: String): BackhaulDraft? {
        val hit = (cache.get(code) as BackhaulDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulDraft> = inner.list()
}

fun newCachedBackhaulDraftStore(inner: BackhaulDraftStore, ttl: Int, clock: Clock): BackhaulDraftStore {
    return CachedBackhaulDraftStore(
        inner = inner,
        cache = newTtlCache<BackhaulDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul drafts. Empty fields match all. */
data class BackhaulDraftQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulDraft>): MutableList<BackhaulDraft> {
        val matched = filter(items) { candidate: BackhaulDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulDrafts(store: BackhaulDraftStore, query: BackhaulDraftQuery): MutableList<BackhaulDraft> {
    return query.apply(store.list())
}
