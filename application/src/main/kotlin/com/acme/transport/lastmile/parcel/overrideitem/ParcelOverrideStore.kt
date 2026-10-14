package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel overrides by code. */
interface ParcelOverrideStore {
    fun find(code: String): ParcelOverride?
    fun save(item: ParcelOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelOverrideStore(
    var items: MutableMap<String, ParcelOverride>,
    var order: MutableList<String>,
) : ParcelOverrideStore {
    override fun find(code: String): ParcelOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelOverride) {
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

    override fun list(): MutableList<ParcelOverride> {
        val out = mutableListOf<ParcelOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelOverrideStore(): ParcelOverrideStore {
    return MemoryParcelOverrideStore(mutableMapOf<String, ParcelOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelOverrideStore(
    var inner: ParcelOverrideStore,
    var cache: TtlCache<ParcelOverride>,
    var hits: Int,
    var misses: Int,
) : ParcelOverrideStore {
    override fun find(code: String): ParcelOverride? {
        val hit = (cache.get(code) as ParcelOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelOverride> = inner.list()
}

fun newCachedParcelOverrideStore(inner: ParcelOverrideStore, ttl: Int, clock: Clock): ParcelOverrideStore {
    return CachedParcelOverrideStore(
        inner = inner,
        cache = newTtlCache<ParcelOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel overrides. Empty fields match all. */
data class ParcelOverrideQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelOverride>): MutableList<ParcelOverride> {
        val matched = filter(items) { candidate: ParcelOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelOverrides(store: ParcelOverrideStore, query: ParcelOverrideQuery): MutableList<ParcelOverride> {
    return query.apply(store.list())
}
