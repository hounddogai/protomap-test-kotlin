package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay variances by code. */
interface RelayVarianceStore {
    fun find(code: String): RelayVariance?
    fun save(item: RelayVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayVarianceStore(
    var items: MutableMap<String, RelayVariance>,
    var order: MutableList<String>,
) : RelayVarianceStore {
    override fun find(code: String): RelayVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayVariance) {
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

    override fun list(): MutableList<RelayVariance> {
        val out = mutableListOf<RelayVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayVarianceStore(): RelayVarianceStore {
    return MemoryRelayVarianceStore(mutableMapOf<String, RelayVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayVarianceStore(
    var inner: RelayVarianceStore,
    var cache: TtlCache<RelayVariance>,
    var hits: Int,
    var misses: Int,
) : RelayVarianceStore {
    override fun find(code: String): RelayVariance? {
        val hit = (cache.get(code) as RelayVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayVariance> = inner.list()
}

fun newCachedRelayVarianceStore(inner: RelayVarianceStore, ttl: Int, clock: Clock): RelayVarianceStore {
    return CachedRelayVarianceStore(
        inner = inner,
        cache = newTtlCache<RelayVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay variances. Empty fields match all. */
data class RelayVarianceQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RelayVariance>): MutableList<RelayVariance> {
        val matched = filter(items) { candidate: RelayVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayVariances(store: RelayVarianceStore, query: RelayVarianceQuery): MutableList<RelayVariance> {
    return query.apply(store.list())
}
