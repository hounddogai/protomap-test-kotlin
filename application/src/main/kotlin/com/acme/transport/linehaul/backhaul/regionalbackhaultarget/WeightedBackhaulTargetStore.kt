package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted backhaul targets by code. */
interface WeightedBackhaulTargetStore {
    fun find(code: String): WeightedBackhaulTarget?
    fun save(item: WeightedBackhaulTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedBackhaulTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedBackhaulTargetStore(
    var items: MutableMap<String, WeightedBackhaulTarget>,
    var order: MutableList<String>,
) : WeightedBackhaulTargetStore {
    override fun find(code: String): WeightedBackhaulTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedBackhaulTarget) {
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

    override fun list(): MutableList<WeightedBackhaulTarget> {
        val out = mutableListOf<WeightedBackhaulTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedBackhaulTargetStore(): WeightedBackhaulTargetStore {
    return MemoryWeightedBackhaulTargetStore(
        mutableMapOf<String, WeightedBackhaulTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedBackhaulTargetStore(
    var inner: WeightedBackhaulTargetStore,
    var cache: TtlCache<WeightedBackhaulTarget>,
    var hits: Int,
    var misses: Int,
) : WeightedBackhaulTargetStore {
    override fun find(code: String): WeightedBackhaulTarget? {
        val hit = (cache.get(code) as WeightedBackhaulTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedBackhaulTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedBackhaulTarget> = inner.list()
}

fun newCachedWeightedBackhaulTargetStore(inner: WeightedBackhaulTargetStore, ttl: Int, clock: Clock): WeightedBackhaulTargetStore {
    return CachedWeightedBackhaulTargetStore(
        inner = inner,
        cache = newTtlCache<WeightedBackhaulTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted backhaul targets. Empty fields match all. */
data class WeightedBackhaulTargetQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedBackhaulTarget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<WeightedBackhaulTarget>): MutableList<WeightedBackhaulTarget> {
        val matched = filter(items) { candidate: WeightedBackhaulTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedBackhaulTargets(store: WeightedBackhaulTargetStore, query: WeightedBackhaulTargetQuery): MutableList<WeightedBackhaulTarget> {
    return query.apply(store.list())
}
