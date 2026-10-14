package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy accessorial plans by code. */
interface LegacyAccessorialPlanStore {
    fun find(code: String): LegacyAccessorialPlan?
    fun save(item: LegacyAccessorialPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAccessorialPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAccessorialPlanStore(
    var items: MutableMap<String, LegacyAccessorialPlan>,
    var order: MutableList<String>,
) : LegacyAccessorialPlanStore {
    override fun find(code: String): LegacyAccessorialPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAccessorialPlan) {
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

    override fun list(): MutableList<LegacyAccessorialPlan> {
        val out = mutableListOf<LegacyAccessorialPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAccessorialPlanStore(): LegacyAccessorialPlanStore {
    return MemoryLegacyAccessorialPlanStore(
        mutableMapOf<String, LegacyAccessorialPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAccessorialPlanStore(
    var inner: LegacyAccessorialPlanStore,
    var cache: TtlCache<LegacyAccessorialPlan>,
    var hits: Int,
    var misses: Int,
) : LegacyAccessorialPlanStore {
    override fun find(code: String): LegacyAccessorialPlan? {
        val hit = (cache.get(code) as LegacyAccessorialPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAccessorialPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAccessorialPlan> = inner.list()
}

fun newCachedLegacyAccessorialPlanStore(inner: LegacyAccessorialPlanStore, ttl: Int, clock: Clock): LegacyAccessorialPlanStore {
    return CachedLegacyAccessorialPlanStore(
        inner = inner,
        cache = newTtlCache<LegacyAccessorialPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy accessorial plans. Empty fields match all. */
data class LegacyAccessorialPlanQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAccessorialPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAccessorialPlan>): MutableList<LegacyAccessorialPlan> {
        val matched = filter(items) { candidate: LegacyAccessorialPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAccessorialPlans(store: LegacyAccessorialPlanStore, query: LegacyAccessorialPlanQuery): MutableList<LegacyAccessorialPlan> {
    return query.apply(store.list())
}
