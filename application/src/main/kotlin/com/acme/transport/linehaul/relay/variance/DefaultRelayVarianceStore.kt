package com.acme.transport.linehaul.relay.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default relay variances by code. */
interface DefaultRelayVarianceStore {
    fun find(code: String): DefaultRelayVariance?
    fun save(item: DefaultRelayVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRelayVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRelayVarianceStore(
    var items: MutableMap<String, DefaultRelayVariance>,
    var order: MutableList<String>,
) : DefaultRelayVarianceStore {
    override fun find(code: String): DefaultRelayVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRelayVariance) {
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

    override fun list(): MutableList<DefaultRelayVariance> {
        val out = mutableListOf<DefaultRelayVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRelayVarianceStore(): DefaultRelayVarianceStore {
    return MemoryDefaultRelayVarianceStore(
        mutableMapOf<String, DefaultRelayVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRelayVarianceStore(
    var inner: DefaultRelayVarianceStore,
    var cache: TtlCache<DefaultRelayVariance>,
    var hits: Int,
    var misses: Int,
) : DefaultRelayVarianceStore {
    override fun find(code: String): DefaultRelayVariance? {
        val hit = (cache.get(code) as DefaultRelayVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRelayVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRelayVariance> = inner.list()
}

fun newCachedDefaultRelayVarianceStore(inner: DefaultRelayVarianceStore, ttl: Int, clock: Clock): DefaultRelayVarianceStore {
    return CachedDefaultRelayVarianceStore(
        inner = inner,
        cache = newTtlCache<DefaultRelayVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default relay variances. Empty fields match all. */
data class DefaultRelayVarianceQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRelayVariance): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRelayVariance>): MutableList<DefaultRelayVariance> {
        val matched = filter(items) { candidate: DefaultRelayVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRelayVariances(store: DefaultRelayVarianceStore, query: DefaultRelayVarianceQuery): MutableList<DefaultRelayVariance> {
    return query.apply(store.list())
}
