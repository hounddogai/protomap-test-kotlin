package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker digests by code. */
interface BrokerDigestStore {
    fun find(code: String): BrokerDigest?
    fun save(item: BrokerDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerDigestStore(
    var items: MutableMap<String, BrokerDigest>,
    var order: MutableList<String>,
) : BrokerDigestStore {
    override fun find(code: String): BrokerDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerDigest) {
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

    override fun list(): MutableList<BrokerDigest> {
        val out = mutableListOf<BrokerDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerDigestStore(): BrokerDigestStore {
    return MemoryBrokerDigestStore(mutableMapOf<String, BrokerDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerDigestStore(
    var inner: BrokerDigestStore,
    var cache: TtlCache<BrokerDigest>,
    var hits: Int,
    var misses: Int,
) : BrokerDigestStore {
    override fun find(code: String): BrokerDigest? {
        val hit = (cache.get(code) as BrokerDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerDigest> = inner.list()
}

fun newCachedBrokerDigestStore(inner: BrokerDigestStore, ttl: Int, clock: Clock): BrokerDigestStore {
    return CachedBrokerDigestStore(
        inner = inner,
        cache = newTtlCache<BrokerDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker digests. Empty fields match all. */
data class BrokerDigestQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerDigest>): MutableList<BrokerDigest> {
        val matched = filter(items) { candidate: BrokerDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerDigests(store: BrokerDigestStore, query: BrokerDigestQuery): MutableList<BrokerDigest> {
    return query.apply(store.list())
}
