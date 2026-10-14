package com.acme.transport.customs.broker.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default broker digests by code. */
interface DefaultBrokerDigestStore {
    fun find(code: String): DefaultBrokerDigest?
    fun save(item: DefaultBrokerDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultBrokerDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultBrokerDigestStore(
    var items: MutableMap<String, DefaultBrokerDigest>,
    var order: MutableList<String>,
) : DefaultBrokerDigestStore {
    override fun find(code: String): DefaultBrokerDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultBrokerDigest) {
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

    override fun list(): MutableList<DefaultBrokerDigest> {
        val out = mutableListOf<DefaultBrokerDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultBrokerDigestStore(): DefaultBrokerDigestStore {
    return MemoryDefaultBrokerDigestStore(mutableMapOf<String, DefaultBrokerDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultBrokerDigestStore(
    var inner: DefaultBrokerDigestStore,
    var cache: TtlCache<DefaultBrokerDigest>,
    var hits: Int,
    var misses: Int,
) : DefaultBrokerDigestStore {
    override fun find(code: String): DefaultBrokerDigest? {
        val hit = (cache.get(code) as DefaultBrokerDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultBrokerDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultBrokerDigest> = inner.list()
}

fun newCachedDefaultBrokerDigestStore(inner: DefaultBrokerDigestStore, ttl: Int, clock: Clock): DefaultBrokerDigestStore {
    return CachedDefaultBrokerDigestStore(
        inner = inner,
        cache = newTtlCache<DefaultBrokerDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default broker digests. Empty fields match all. */
data class DefaultBrokerDigestQuery(
    var lane: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultBrokerDigest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DefaultBrokerDigest>): MutableList<DefaultBrokerDigest> {
        val matched = filter(items) { candidate: DefaultBrokerDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultBrokerDigests(store: DefaultBrokerDigestStore, query: DefaultBrokerDigestQuery): MutableList<DefaultBrokerDigest> {
    return query.apply(store.list())
}
