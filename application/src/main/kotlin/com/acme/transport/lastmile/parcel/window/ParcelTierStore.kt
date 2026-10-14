package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel tiers by code. */
interface ParcelTierStore {
    fun find(code: String): ParcelTier?
    fun save(item: ParcelTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelTierStore(
    var items: MutableMap<String, ParcelTier>,
    var order: MutableList<String>,
) : ParcelTierStore {
    override fun find(code: String): ParcelTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelTier) {
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

    override fun list(): MutableList<ParcelTier> {
        val out = mutableListOf<ParcelTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelTierStore(): ParcelTierStore {
    return MemoryParcelTierStore(mutableMapOf<String, ParcelTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelTierStore(
    var inner: ParcelTierStore,
    var cache: TtlCache<ParcelTier>,
    var hits: Int,
    var misses: Int,
) : ParcelTierStore {
    override fun find(code: String): ParcelTier? {
        val hit = (cache.get(code) as ParcelTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelTier> = inner.list()
}

fun newCachedParcelTierStore(inner: ParcelTierStore, ttl: Int, clock: Clock): ParcelTierStore {
    return CachedParcelTierStore(
        inner = inner,
        cache = newTtlCache<ParcelTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel tiers. Empty fields match all. */
data class ParcelTierQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelTier>): MutableList<ParcelTier> {
        val matched = filter(items) { candidate: ParcelTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelTiers(store: ParcelTierStore, query: ParcelTierQuery): MutableList<ParcelTier> {
    return query.apply(store.list())
}
