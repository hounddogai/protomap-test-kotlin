package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff targets by code. */
interface CutoffTargetStore {
    fun find(code: String): CutoffTarget?
    fun save(item: CutoffTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffTargetStore(
    var items: MutableMap<String, CutoffTarget>,
    var order: MutableList<String>,
) : CutoffTargetStore {
    override fun find(code: String): CutoffTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffTarget) {
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

    override fun list(): MutableList<CutoffTarget> {
        val out = mutableListOf<CutoffTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffTargetStore(): CutoffTargetStore {
    return MemoryCutoffTargetStore(mutableMapOf<String, CutoffTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffTargetStore(
    var inner: CutoffTargetStore,
    var cache: TtlCache<CutoffTarget>,
    var hits: Int,
    var misses: Int,
) : CutoffTargetStore {
    override fun find(code: String): CutoffTarget? {
        val hit = (cache.get(code) as CutoffTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffTarget> = inner.list()
}

fun newCachedCutoffTargetStore(inner: CutoffTargetStore, ttl: Int, clock: Clock): CutoffTargetStore {
    return CachedCutoffTargetStore(
        inner = inner,
        cache = newTtlCache<CutoffTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff targets. Empty fields match all. */
data class CutoffTargetQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffTarget>): MutableList<CutoffTarget> {
        val matched = filter(items) { candidate: CutoffTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffTargets(store: CutoffTargetStore, query: CutoffTargetQuery): MutableList<CutoffTarget> {
    return query.apply(store.list())
}
