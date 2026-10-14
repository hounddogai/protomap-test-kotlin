package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker plans by code. */
interface LockerPlanStore {
    fun find(code: String): LockerPlan?
    fun save(item: LockerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerPlanStore(
    var items: MutableMap<String, LockerPlan>,
    var order: MutableList<String>,
) : LockerPlanStore {
    override fun find(code: String): LockerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerPlan) {
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

    override fun list(): MutableList<LockerPlan> {
        val out = mutableListOf<LockerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerPlanStore(): LockerPlanStore {
    return MemoryLockerPlanStore(mutableMapOf<String, LockerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerPlanStore(
    var inner: LockerPlanStore,
    var cache: TtlCache<LockerPlan>,
    var hits: Int,
    var misses: Int,
) : LockerPlanStore {
    override fun find(code: String): LockerPlan? {
        val hit = (cache.get(code) as LockerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerPlan> = inner.list()
}

fun newCachedLockerPlanStore(inner: LockerPlanStore, ttl: Int, clock: Clock): LockerPlanStore {
    return CachedLockerPlanStore(
        inner = inner,
        cache = newTtlCache<LockerPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker plans. Empty fields match all. */
data class LockerPlanQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerPlan): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerPlan>): MutableList<LockerPlan> {
        val matched = filter(items) { candidate: LockerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerPlans(store: LockerPlanStore, query: LockerPlanQuery): MutableList<LockerPlan> {
    return query.apply(store.list())
}
