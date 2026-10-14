package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard plans by code. */
interface TrailerYardPlanStore {
    fun find(code: String): TrailerYardPlan?
    fun save(item: TrailerYardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardPlanStore(
    var items: MutableMap<String, TrailerYardPlan>,
    var order: MutableList<String>,
) : TrailerYardPlanStore {
    override fun find(code: String): TrailerYardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardPlan) {
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

    override fun list(): MutableList<TrailerYardPlan> {
        val out = mutableListOf<TrailerYardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardPlanStore(): TrailerYardPlanStore {
    return MemoryTrailerYardPlanStore(mutableMapOf<String, TrailerYardPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardPlanStore(
    var inner: TrailerYardPlanStore,
    var cache: TtlCache<TrailerYardPlan>,
    var hits: Int,
    var misses: Int,
) : TrailerYardPlanStore {
    override fun find(code: String): TrailerYardPlan? {
        val hit = (cache.get(code) as TrailerYardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardPlan> = inner.list()
}

fun newCachedTrailerYardPlanStore(inner: TrailerYardPlanStore, ttl: Int, clock: Clock): TrailerYardPlanStore {
    return CachedTrailerYardPlanStore(
        inner = inner,
        cache = newTtlCache<TrailerYardPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard plans. Empty fields match all. */
data class TrailerYardPlanQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardPlan): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<TrailerYardPlan>): MutableList<TrailerYardPlan> {
        val matched = filter(items) { candidate: TrailerYardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardPlans(store: TrailerYardPlanStore, query: TrailerYardPlanQuery): MutableList<TrailerYardPlan> {
    return query.apply(store.list())
}
