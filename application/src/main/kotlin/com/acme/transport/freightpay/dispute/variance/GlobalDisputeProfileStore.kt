package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global dispute profiles by code. */
interface GlobalDisputeProfileStore {
    fun find(code: String): GlobalDisputeProfile?
    fun save(item: GlobalDisputeProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDisputeProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDisputeProfileStore(
    var items: MutableMap<String, GlobalDisputeProfile>,
    var order: MutableList<String>,
) : GlobalDisputeProfileStore {
    override fun find(code: String): GlobalDisputeProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDisputeProfile) {
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

    override fun list(): MutableList<GlobalDisputeProfile> {
        val out = mutableListOf<GlobalDisputeProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDisputeProfileStore(): GlobalDisputeProfileStore {
    return MemoryGlobalDisputeProfileStore(
        mutableMapOf<String, GlobalDisputeProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDisputeProfileStore(
    var inner: GlobalDisputeProfileStore,
    var cache: TtlCache<GlobalDisputeProfile>,
    var hits: Int,
    var misses: Int,
) : GlobalDisputeProfileStore {
    override fun find(code: String): GlobalDisputeProfile? {
        val hit = (cache.get(code) as GlobalDisputeProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDisputeProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDisputeProfile> = inner.list()
}

fun newCachedGlobalDisputeProfileStore(inner: GlobalDisputeProfileStore, ttl: Int, clock: Clock): GlobalDisputeProfileStore {
    return CachedGlobalDisputeProfileStore(
        inner = inner,
        cache = newTtlCache<GlobalDisputeProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global dispute profiles. Empty fields match all. */
data class GlobalDisputeProfileQuery(
    var region: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDisputeProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDisputeProfile>): MutableList<GlobalDisputeProfile> {
        val matched = filter(items) { candidate: GlobalDisputeProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDisputeProfiles(store: GlobalDisputeProfileStore, query: GlobalDisputeProfileQuery): MutableList<GlobalDisputeProfile> {
    return query.apply(store.list())
}
