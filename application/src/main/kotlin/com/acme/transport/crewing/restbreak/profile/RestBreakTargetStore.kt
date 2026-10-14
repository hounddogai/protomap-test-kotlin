package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break targets by code. */
interface RestBreakTargetStore {
    fun find(code: String): RestBreakTarget?
    fun save(item: RestBreakTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakTargetStore(
    var items: MutableMap<String, RestBreakTarget>,
    var order: MutableList<String>,
) : RestBreakTargetStore {
    override fun find(code: String): RestBreakTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakTarget) {
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

    override fun list(): MutableList<RestBreakTarget> {
        val out = mutableListOf<RestBreakTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakTargetStore(): RestBreakTargetStore {
    return MemoryRestBreakTargetStore(mutableMapOf<String, RestBreakTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakTargetStore(
    var inner: RestBreakTargetStore,
    var cache: TtlCache<RestBreakTarget>,
    var hits: Int,
    var misses: Int,
) : RestBreakTargetStore {
    override fun find(code: String): RestBreakTarget? {
        val hit = (cache.get(code) as RestBreakTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakTarget> = inner.list()
}

fun newCachedRestBreakTargetStore(inner: RestBreakTargetStore, ttl: Int, clock: Clock): RestBreakTargetStore {
    return CachedRestBreakTargetStore(
        inner = inner,
        cache = newTtlCache<RestBreakTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break targets. Empty fields match all. */
data class RestBreakTargetQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<RestBreakTarget>): MutableList<RestBreakTarget> {
        val matched = filter(items) { candidate: RestBreakTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakTargets(store: RestBreakTargetStore, query: RestBreakTargetQuery): MutableList<RestBreakTarget> {
    return query.apply(store.list())
}
