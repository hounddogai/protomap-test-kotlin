package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default emission factor adjustments by code. */
interface DefaultEmissionFactorAdjustmentStore {
    fun find(code: String): DefaultEmissionFactorAdjustment?
    fun save(item: DefaultEmissionFactorAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultEmissionFactorAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultEmissionFactorAdjustmentStore(
    var items: MutableMap<String, DefaultEmissionFactorAdjustment>,
    var order: MutableList<String>,
) : DefaultEmissionFactorAdjustmentStore {
    override fun find(code: String): DefaultEmissionFactorAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultEmissionFactorAdjustment) {
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

    override fun list(): MutableList<DefaultEmissionFactorAdjustment> {
        val out = mutableListOf<DefaultEmissionFactorAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultEmissionFactorAdjustmentStore(): DefaultEmissionFactorAdjustmentStore {
    return MemoryDefaultEmissionFactorAdjustmentStore(
        mutableMapOf<String, DefaultEmissionFactorAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultEmissionFactorAdjustmentStore(
    var inner: DefaultEmissionFactorAdjustmentStore,
    var cache: TtlCache<DefaultEmissionFactorAdjustment>,
    var hits: Int,
    var misses: Int,
) : DefaultEmissionFactorAdjustmentStore {
    override fun find(code: String): DefaultEmissionFactorAdjustment? {
        val hit = (cache.get(code) as DefaultEmissionFactorAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultEmissionFactorAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultEmissionFactorAdjustment> = inner.list()
}

fun newCachedDefaultEmissionFactorAdjustmentStore(inner: DefaultEmissionFactorAdjustmentStore, ttl: Int, clock: Clock): DefaultEmissionFactorAdjustmentStore {
    return CachedDefaultEmissionFactorAdjustmentStore(
        inner = inner,
        cache = newTtlCache<DefaultEmissionFactorAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default emission factor adjustments. Empty fields match all. */
data class DefaultEmissionFactorAdjustmentQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultEmissionFactorAdjustment): Boolean {
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

    fun apply(items: MutableList<DefaultEmissionFactorAdjustment>): MutableList<DefaultEmissionFactorAdjustment> {
        val matched = filter(items) { candidate: DefaultEmissionFactorAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultEmissionFactorAdjustments(store: DefaultEmissionFactorAdjustmentStore, query: DefaultEmissionFactorAdjustmentQuery): MutableList<DefaultEmissionFactorAdjustment> {
    return query.apply(store.list())
}
