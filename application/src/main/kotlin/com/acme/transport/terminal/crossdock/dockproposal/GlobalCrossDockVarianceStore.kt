package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global cross dock variances by code. */
interface GlobalCrossDockVarianceStore {
    fun find(code: String): GlobalCrossDockVariance?
    fun save(item: GlobalCrossDockVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCrossDockVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCrossDockVarianceStore(
    var items: MutableMap<String, GlobalCrossDockVariance>,
    var order: MutableList<String>,
) : GlobalCrossDockVarianceStore {
    override fun find(code: String): GlobalCrossDockVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCrossDockVariance) {
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

    override fun list(): MutableList<GlobalCrossDockVariance> {
        val out = mutableListOf<GlobalCrossDockVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCrossDockVarianceStore(): GlobalCrossDockVarianceStore {
    return MemoryGlobalCrossDockVarianceStore(
        mutableMapOf<String, GlobalCrossDockVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCrossDockVarianceStore(
    var inner: GlobalCrossDockVarianceStore,
    var cache: TtlCache<GlobalCrossDockVariance>,
    var hits: Int,
    var misses: Int,
) : GlobalCrossDockVarianceStore {
    override fun find(code: String): GlobalCrossDockVariance? {
        val hit = (cache.get(code) as GlobalCrossDockVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCrossDockVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCrossDockVariance> = inner.list()
}

fun newCachedGlobalCrossDockVarianceStore(inner: GlobalCrossDockVarianceStore, ttl: Int, clock: Clock): GlobalCrossDockVarianceStore {
    return CachedGlobalCrossDockVarianceStore(
        inner = inner,
        cache = newTtlCache<GlobalCrossDockVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cross dock variances. Empty fields match all. */
data class GlobalCrossDockVarianceQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCrossDockVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCrossDockVariance>): MutableList<GlobalCrossDockVariance> {
        val matched = filter(items) { candidate: GlobalCrossDockVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCrossDockVariances(store: GlobalCrossDockVarianceStore, query: GlobalCrossDockVarianceQuery): MutableList<GlobalCrossDockVariance> {
    return query.apply(store.list())
}
