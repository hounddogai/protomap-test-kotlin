package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped tire plans by code. */
interface CappedTirePlanStore {
    fun find(code: String): CappedTirePlan?
    fun save(item: CappedTirePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTirePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTirePlanStore(
    var items: MutableMap<String, CappedTirePlan>,
    var order: MutableList<String>,
) : CappedTirePlanStore {
    override fun find(code: String): CappedTirePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTirePlan) {
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

    override fun list(): MutableList<CappedTirePlan> {
        val out = mutableListOf<CappedTirePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTirePlanStore(): CappedTirePlanStore {
    return MemoryCappedTirePlanStore(mutableMapOf<String, CappedTirePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTirePlanStore(
    var inner: CappedTirePlanStore,
    var cache: TtlCache<CappedTirePlan>,
    var hits: Int,
    var misses: Int,
) : CappedTirePlanStore {
    override fun find(code: String): CappedTirePlan? {
        val hit = (cache.get(code) as CappedTirePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTirePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTirePlan> = inner.list()
}

fun newCachedCappedTirePlanStore(inner: CappedTirePlanStore, ttl: Int, clock: Clock): CappedTirePlanStore {
    return CachedCappedTirePlanStore(
        inner = inner,
        cache = newTtlCache<CappedTirePlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tire plans. Empty fields match all. */
data class CappedTirePlanQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTirePlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTirePlan>): MutableList<CappedTirePlan> {
        val matched = filter(items) { candidate: CappedTirePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTirePlans(store: CappedTirePlanStore, query: CappedTirePlanQuery): MutableList<CappedTirePlan> {
    return query.apply(store.list())
}
