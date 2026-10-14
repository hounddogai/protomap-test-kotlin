package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial drafts by code. */
interface AccessorialDraftStore {
    fun find(code: String): AccessorialDraft?
    fun save(item: AccessorialDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialDraftStore(
    var items: MutableMap<String, AccessorialDraft>,
    var order: MutableList<String>,
) : AccessorialDraftStore {
    override fun find(code: String): AccessorialDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialDraft) {
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

    override fun list(): MutableList<AccessorialDraft> {
        val out = mutableListOf<AccessorialDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialDraftStore(): AccessorialDraftStore {
    return MemoryAccessorialDraftStore(mutableMapOf<String, AccessorialDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialDraftStore(
    var inner: AccessorialDraftStore,
    var cache: TtlCache<AccessorialDraft>,
    var hits: Int,
    var misses: Int,
) : AccessorialDraftStore {
    override fun find(code: String): AccessorialDraft? {
        val hit = (cache.get(code) as AccessorialDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialDraft> = inner.list()
}

fun newCachedAccessorialDraftStore(inner: AccessorialDraftStore, ttl: Int, clock: Clock): AccessorialDraftStore {
    return CachedAccessorialDraftStore(
        inner = inner,
        cache = newTtlCache<AccessorialDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial drafts. Empty fields match all. */
data class AccessorialDraftQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialDraft>): MutableList<AccessorialDraft> {
        val matched = filter(items) { candidate: AccessorialDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialDrafts(store: AccessorialDraftStore, query: AccessorialDraftQuery): MutableList<AccessorialDraft> {
    return query.apply(store.list())
}
