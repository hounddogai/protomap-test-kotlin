package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global relay variances by code. */
interface GlobalRelayVarianceStore {
    fun find(code: String): GlobalRelayVariance?
    fun save(item: GlobalRelayVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRelayVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRelayVarianceStore(
    var items: MutableMap<String, GlobalRelayVariance>,
    var order: MutableList<String>,
) : GlobalRelayVarianceStore {
    override fun find(code: String): GlobalRelayVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRelayVariance) {
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

    override fun list(): MutableList<GlobalRelayVariance> {
        val out = mutableListOf<GlobalRelayVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRelayVarianceStore(): GlobalRelayVarianceStore {
    return MemoryGlobalRelayVarianceStore(mutableMapOf<String, GlobalRelayVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRelayVarianceStore(
    var inner: GlobalRelayVarianceStore,
    var cache: TtlCache<GlobalRelayVariance>,
    var hits: Int,
    var misses: Int,
) : GlobalRelayVarianceStore {
    override fun find(code: String): GlobalRelayVariance? {
        val hit = (cache.get(code) as GlobalRelayVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRelayVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRelayVariance> = inner.list()
}

fun newCachedGlobalRelayVarianceStore(inner: GlobalRelayVarianceStore, ttl: Int, clock: Clock): GlobalRelayVarianceStore {
    return CachedGlobalRelayVarianceStore(
        inner = inner,
        cache = newTtlCache<GlobalRelayVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global relay variances. Empty fields match all. */
data class GlobalRelayVarianceQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRelayVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRelayVariance>): MutableList<GlobalRelayVariance> {
        val matched = filter(items) { candidate: GlobalRelayVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRelayVariances(store: GlobalRelayVarianceStore, query: GlobalRelayVarianceQuery): MutableList<GlobalRelayVariance> {
    return query.apply(store.list())
}
