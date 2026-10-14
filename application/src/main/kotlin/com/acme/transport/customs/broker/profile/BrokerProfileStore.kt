package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker profiles by code. */
interface BrokerProfileStore {
    fun find(code: String): BrokerProfile?
    fun save(item: BrokerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerProfileStore(
    var items: MutableMap<String, BrokerProfile>,
    var order: MutableList<String>,
) : BrokerProfileStore {
    override fun find(code: String): BrokerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerProfile) {
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

    override fun list(): MutableList<BrokerProfile> {
        val out = mutableListOf<BrokerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerProfileStore(): BrokerProfileStore {
    return MemoryBrokerProfileStore(mutableMapOf<String, BrokerProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerProfileStore(
    var inner: BrokerProfileStore,
    var cache: TtlCache<BrokerProfile>,
    var hits: Int,
    var misses: Int,
) : BrokerProfileStore {
    override fun find(code: String): BrokerProfile? {
        val hit = (cache.get(code) as BrokerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerProfile> = inner.list()
}

fun newCachedBrokerProfileStore(inner: BrokerProfileStore, ttl: Int, clock: Clock): BrokerProfileStore {
    return CachedBrokerProfileStore(
        inner = inner,
        cache = newTtlCache<BrokerProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker profiles. Empty fields match all. */
data class BrokerProfileQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerProfile>): MutableList<BrokerProfile> {
        val matched = filter(items) { candidate: BrokerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerProfiles(store: BrokerProfileStore, query: BrokerProfileQuery): MutableList<BrokerProfile> {
    return query.apply(store.list())
}
