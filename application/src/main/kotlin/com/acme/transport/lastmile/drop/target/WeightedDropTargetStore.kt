package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted drop targets by code. */
interface WeightedDropTargetStore {
    fun find(code: String): WeightedDropTarget?
    fun save(item: WeightedDropTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDropTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDropTargetStore(
    var items: MutableMap<String, WeightedDropTarget>,
    var order: MutableList<String>,
) : WeightedDropTargetStore {
    override fun find(code: String): WeightedDropTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDropTarget) {
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

    override fun list(): MutableList<WeightedDropTarget> {
        val out = mutableListOf<WeightedDropTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDropTargetStore(): WeightedDropTargetStore {
    return MemoryWeightedDropTargetStore(mutableMapOf<String, WeightedDropTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDropTargetStore(
    var inner: WeightedDropTargetStore,
    var cache: TtlCache<WeightedDropTarget>,
    var hits: Int,
    var misses: Int,
) : WeightedDropTargetStore {
    override fun find(code: String): WeightedDropTarget? {
        val hit = (cache.get(code) as WeightedDropTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDropTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDropTarget> = inner.list()
}

fun newCachedWeightedDropTargetStore(inner: WeightedDropTargetStore, ttl: Int, clock: Clock): WeightedDropTargetStore {
    return CachedWeightedDropTargetStore(
        inner = inner,
        cache = newTtlCache<WeightedDropTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted drop targets. Empty fields match all. */
data class WeightedDropTargetQuery(
    var channel: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDropTarget): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDropTarget>): MutableList<WeightedDropTarget> {
        val matched = filter(items) { candidate: WeightedDropTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDropTargets(store: WeightedDropTargetStore, query: WeightedDropTargetQuery): MutableList<WeightedDropTarget> {
    return query.apply(store.list())
}
