package com.acme.transport.customs.clearance.tier

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered clearance drafts by code. */
interface TieredClearanceDraftStore {
    fun find(code: String): TieredClearanceDraft?
    fun save(item: TieredClearanceDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredClearanceDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredClearanceDraftStore(
    var items: MutableMap<String, TieredClearanceDraft>,
    var order: MutableList<String>,
) : TieredClearanceDraftStore {
    override fun find(code: String): TieredClearanceDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredClearanceDraft) {
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

    override fun list(): MutableList<TieredClearanceDraft> {
        val out = mutableListOf<TieredClearanceDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredClearanceDraftStore(): TieredClearanceDraftStore {
    return MemoryTieredClearanceDraftStore(
        mutableMapOf<String, TieredClearanceDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredClearanceDraftStore(
    var inner: TieredClearanceDraftStore,
    var cache: TtlCache<TieredClearanceDraft>,
    var hits: Int,
    var misses: Int,
) : TieredClearanceDraftStore {
    override fun find(code: String): TieredClearanceDraft? {
        val hit = (cache.get(code) as TieredClearanceDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredClearanceDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredClearanceDraft> = inner.list()
}

fun newCachedTieredClearanceDraftStore(inner: TieredClearanceDraftStore, ttl: Int, clock: Clock): TieredClearanceDraftStore {
    return CachedTieredClearanceDraftStore(
        inner = inner,
        cache = newTtlCache<TieredClearanceDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered clearance drafts. Empty fields match all. */
data class TieredClearanceDraftQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredClearanceDraft): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredClearanceDraft>): MutableList<TieredClearanceDraft> {
        val matched = filter(items) { candidate: TieredClearanceDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredClearanceDrafts(store: TieredClearanceDraftStore, query: TieredClearanceDraftQuery): MutableList<TieredClearanceDraft> {
    return query.apply(store.list())
}
