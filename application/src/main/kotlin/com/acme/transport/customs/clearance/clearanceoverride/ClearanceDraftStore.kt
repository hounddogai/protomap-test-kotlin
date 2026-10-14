package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance drafts by code. */
interface ClearanceDraftStore {
    fun find(code: String): ClearanceDraft?
    fun save(item: ClearanceDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceDraftStore(
    var items: MutableMap<String, ClearanceDraft>,
    var order: MutableList<String>,
) : ClearanceDraftStore {
    override fun find(code: String): ClearanceDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceDraft) {
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

    override fun list(): MutableList<ClearanceDraft> {
        val out = mutableListOf<ClearanceDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceDraftStore(): ClearanceDraftStore {
    return MemoryClearanceDraftStore(mutableMapOf<String, ClearanceDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceDraftStore(
    var inner: ClearanceDraftStore,
    var cache: TtlCache<ClearanceDraft>,
    var hits: Int,
    var misses: Int,
) : ClearanceDraftStore {
    override fun find(code: String): ClearanceDraft? {
        val hit = (cache.get(code) as ClearanceDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceDraft> = inner.list()
}

fun newCachedClearanceDraftStore(inner: ClearanceDraftStore, ttl: Int, clock: Clock): ClearanceDraftStore {
    return CachedClearanceDraftStore(
        inner = inner,
        cache = newTtlCache<ClearanceDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance drafts. Empty fields match all. */
data class ClearanceDraftQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceDraft>): MutableList<ClearanceDraft> {
        val matched = filter(items) { candidate: ClearanceDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceDrafts(store: ClearanceDraftStore, query: ClearanceDraftQuery): MutableList<ClearanceDraft> {
    return query.apply(store.list())
}
