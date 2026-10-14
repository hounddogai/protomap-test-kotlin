package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling cutoff variances by code. */
interface RollingCutoffVarianceStore {
    fun find(code: String): RollingCutoffVariance?
    fun save(item: RollingCutoffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCutoffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCutoffVarianceStore(
    var items: MutableMap<String, RollingCutoffVariance>,
    var order: MutableList<String>,
) : RollingCutoffVarianceStore {
    override fun find(code: String): RollingCutoffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCutoffVariance) {
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

    override fun list(): MutableList<RollingCutoffVariance> {
        val out = mutableListOf<RollingCutoffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCutoffVarianceStore(): RollingCutoffVarianceStore {
    return MemoryRollingCutoffVarianceStore(
        mutableMapOf<String, RollingCutoffVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCutoffVarianceStore(
    var inner: RollingCutoffVarianceStore,
    var cache: TtlCache<RollingCutoffVariance>,
    var hits: Int,
    var misses: Int,
) : RollingCutoffVarianceStore {
    override fun find(code: String): RollingCutoffVariance? {
        val hit = (cache.get(code) as RollingCutoffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCutoffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCutoffVariance> = inner.list()
}

fun newCachedRollingCutoffVarianceStore(inner: RollingCutoffVarianceStore, ttl: Int, clock: Clock): RollingCutoffVarianceStore {
    return CachedRollingCutoffVarianceStore(
        inner = inner,
        cache = newTtlCache<RollingCutoffVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling cutoff variances. Empty fields match all. */
data class RollingCutoffVarianceQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCutoffVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCutoffVariance>): MutableList<RollingCutoffVariance> {
        val matched = filter(items) { candidate: RollingCutoffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCutoffVariances(store: RollingCutoffVarianceStore, query: RollingCutoffVarianceQuery): MutableList<RollingCutoffVariance> {
    return query.apply(store.list())
}
