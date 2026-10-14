package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict route offset targets by code. */
interface StrictRouteOffsetTargetStore {
    fun find(code: String): StrictRouteOffsetTarget?
    fun save(item: StrictRouteOffsetTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRouteOffsetTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRouteOffsetTargetStore(
    var items: MutableMap<String, StrictRouteOffsetTarget>,
    var order: MutableList<String>,
) : StrictRouteOffsetTargetStore {
    override fun find(code: String): StrictRouteOffsetTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRouteOffsetTarget) {
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

    override fun list(): MutableList<StrictRouteOffsetTarget> {
        val out = mutableListOf<StrictRouteOffsetTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRouteOffsetTargetStore(): StrictRouteOffsetTargetStore {
    return MemoryStrictRouteOffsetTargetStore(
        mutableMapOf<String, StrictRouteOffsetTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRouteOffsetTargetStore(
    var inner: StrictRouteOffsetTargetStore,
    var cache: TtlCache<StrictRouteOffsetTarget>,
    var hits: Int,
    var misses: Int,
) : StrictRouteOffsetTargetStore {
    override fun find(code: String): StrictRouteOffsetTarget? {
        val hit = (cache.get(code) as StrictRouteOffsetTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRouteOffsetTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRouteOffsetTarget> = inner.list()
}

fun newCachedStrictRouteOffsetTargetStore(inner: StrictRouteOffsetTargetStore, ttl: Int, clock: Clock): StrictRouteOffsetTargetStore {
    return CachedStrictRouteOffsetTargetStore(
        inner = inner,
        cache = newTtlCache<StrictRouteOffsetTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict route offset targets. Empty fields match all. */
data class StrictRouteOffsetTargetQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRouteOffsetTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<StrictRouteOffsetTarget>): MutableList<StrictRouteOffsetTarget> {
        val matched = filter(items) { candidate: StrictRouteOffsetTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRouteOffsetTargets(store: StrictRouteOffsetTargetStore, query: StrictRouteOffsetTargetQuery): MutableList<StrictRouteOffsetTarget> {
    return query.apply(store.list())
}
