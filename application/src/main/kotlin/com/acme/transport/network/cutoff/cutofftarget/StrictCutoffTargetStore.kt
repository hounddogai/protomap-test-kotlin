package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict cutoff targets by code. */
interface StrictCutoffTargetStore {
    fun find(code: String): StrictCutoffTarget?
    fun save(item: StrictCutoffTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCutoffTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCutoffTargetStore(
    var items: MutableMap<String, StrictCutoffTarget>,
    var order: MutableList<String>,
) : StrictCutoffTargetStore {
    override fun find(code: String): StrictCutoffTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCutoffTarget) {
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

    override fun list(): MutableList<StrictCutoffTarget> {
        val out = mutableListOf<StrictCutoffTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCutoffTargetStore(): StrictCutoffTargetStore {
    return MemoryStrictCutoffTargetStore(mutableMapOf<String, StrictCutoffTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCutoffTargetStore(
    var inner: StrictCutoffTargetStore,
    var cache: TtlCache<StrictCutoffTarget>,
    var hits: Int,
    var misses: Int,
) : StrictCutoffTargetStore {
    override fun find(code: String): StrictCutoffTarget? {
        val hit = (cache.get(code) as StrictCutoffTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCutoffTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCutoffTarget> = inner.list()
}

fun newCachedStrictCutoffTargetStore(inner: StrictCutoffTargetStore, ttl: Int, clock: Clock): StrictCutoffTargetStore {
    return CachedStrictCutoffTargetStore(
        inner = inner,
        cache = newTtlCache<StrictCutoffTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict cutoff targets. Empty fields match all. */
data class StrictCutoffTargetQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCutoffTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCutoffTarget>): MutableList<StrictCutoffTarget> {
        val matched = filter(items) { candidate: StrictCutoffTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCutoffTargets(store: StrictCutoffTargetStore, query: StrictCutoffTargetQuery): MutableList<StrictCutoffTarget> {
    return query.apply(store.list())
}
