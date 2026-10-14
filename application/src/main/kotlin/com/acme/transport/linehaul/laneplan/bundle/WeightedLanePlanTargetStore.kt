package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted lane plan targets by code. */
interface WeightedLanePlanTargetStore {
    fun find(code: String): WeightedLanePlanTarget?
    fun save(item: WeightedLanePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLanePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLanePlanTargetStore(
    var items: MutableMap<String, WeightedLanePlanTarget>,
    var order: MutableList<String>,
) : WeightedLanePlanTargetStore {
    override fun find(code: String): WeightedLanePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLanePlanTarget) {
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

    override fun list(): MutableList<WeightedLanePlanTarget> {
        val out = mutableListOf<WeightedLanePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLanePlanTargetStore(): WeightedLanePlanTargetStore {
    return MemoryWeightedLanePlanTargetStore(
        mutableMapOf<String, WeightedLanePlanTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLanePlanTargetStore(
    var inner: WeightedLanePlanTargetStore,
    var cache: TtlCache<WeightedLanePlanTarget>,
    var hits: Int,
    var misses: Int,
) : WeightedLanePlanTargetStore {
    override fun find(code: String): WeightedLanePlanTarget? {
        val hit = (cache.get(code) as WeightedLanePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLanePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLanePlanTarget> = inner.list()
}

fun newCachedWeightedLanePlanTargetStore(inner: WeightedLanePlanTargetStore, ttl: Int, clock: Clock): WeightedLanePlanTargetStore {
    return CachedWeightedLanePlanTargetStore(
        inner = inner,
        cache = newTtlCache<WeightedLanePlanTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lane plan targets. Empty fields match all. */
data class WeightedLanePlanTargetQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLanePlanTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLanePlanTarget>): MutableList<WeightedLanePlanTarget> {
        val matched = filter(items) { candidate: WeightedLanePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLanePlanTargets(store: WeightedLanePlanTargetStore, query: WeightedLanePlanTargetQuery): MutableList<WeightedLanePlanTarget> {
    return query.apply(store.list())
}
