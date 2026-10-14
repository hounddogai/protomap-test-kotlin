package com.acme.transport.fleetops.tire.draft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire drafts by code. */
interface TireDraftStore {
    fun find(code: String): TireDraft?
    fun save(item: TireDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireDraftStore(
    var items: MutableMap<String, TireDraft>,
    var order: MutableList<String>,
) : TireDraftStore {
    override fun find(code: String): TireDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireDraft) {
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

    override fun list(): MutableList<TireDraft> {
        val out = mutableListOf<TireDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireDraftStore(): TireDraftStore {
    return MemoryTireDraftStore(mutableMapOf<String, TireDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireDraftStore(
    var inner: TireDraftStore,
    var cache: TtlCache<TireDraft>,
    var hits: Int,
    var misses: Int,
) : TireDraftStore {
    override fun find(code: String): TireDraft? {
        val hit = (cache.get(code) as TireDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireDraft> = inner.list()
}

fun newCachedTireDraftStore(inner: TireDraftStore, ttl: Int, clock: Clock): TireDraftStore {
    return CachedTireDraftStore(
        inner = inner,
        cache = newTtlCache<TireDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire drafts. Empty fields match all. */
data class TireDraftQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireDraft): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireDraft>): MutableList<TireDraft> {
        val matched = filter(items) { candidate: TireDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireDrafts(store: TireDraftStore, query: TireDraftQuery): MutableList<TireDraft> {
    return query.apply(store.list())
}
