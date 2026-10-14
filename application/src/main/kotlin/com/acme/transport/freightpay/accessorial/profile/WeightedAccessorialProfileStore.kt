package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted accessorial profiles by code. */
interface WeightedAccessorialProfileStore {
    fun find(code: String): WeightedAccessorialProfile?
    fun save(item: WeightedAccessorialProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAccessorialProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAccessorialProfileStore(
    var items: MutableMap<String, WeightedAccessorialProfile>,
    var order: MutableList<String>,
) : WeightedAccessorialProfileStore {
    override fun find(code: String): WeightedAccessorialProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAccessorialProfile) {
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

    override fun list(): MutableList<WeightedAccessorialProfile> {
        val out = mutableListOf<WeightedAccessorialProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAccessorialProfileStore(): WeightedAccessorialProfileStore {
    return MemoryWeightedAccessorialProfileStore(
        mutableMapOf<String, WeightedAccessorialProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAccessorialProfileStore(
    var inner: WeightedAccessorialProfileStore,
    var cache: TtlCache<WeightedAccessorialProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedAccessorialProfileStore {
    override fun find(code: String): WeightedAccessorialProfile? {
        val hit = (cache.get(code) as WeightedAccessorialProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAccessorialProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAccessorialProfile> = inner.list()
}

fun newCachedWeightedAccessorialProfileStore(inner: WeightedAccessorialProfileStore, ttl: Int, clock: Clock): WeightedAccessorialProfileStore {
    return CachedWeightedAccessorialProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedAccessorialProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted accessorial profiles. Empty fields match all. */
data class WeightedAccessorialProfileQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAccessorialProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAccessorialProfile>): MutableList<WeightedAccessorialProfile> {
        val matched = filter(items) { candidate: WeightedAccessorialProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAccessorialProfiles(store: WeightedAccessorialProfileStore, query: WeightedAccessorialProfileQuery): MutableList<WeightedAccessorialProfile> {
    return query.apply(store.list())
}
