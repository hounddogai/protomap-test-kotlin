package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled near miss plans by code. */
interface ScheduledNearMissPlanStore {
    fun find(code: String): ScheduledNearMissPlan?
    fun save(item: ScheduledNearMissPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNearMissPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNearMissPlanStore(
    var items: MutableMap<String, ScheduledNearMissPlan>,
    var order: MutableList<String>,
) : ScheduledNearMissPlanStore {
    override fun find(code: String): ScheduledNearMissPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNearMissPlan) {
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

    override fun list(): MutableList<ScheduledNearMissPlan> {
        val out = mutableListOf<ScheduledNearMissPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNearMissPlanStore(): ScheduledNearMissPlanStore {
    return MemoryScheduledNearMissPlanStore(
        mutableMapOf<String, ScheduledNearMissPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNearMissPlanStore(
    var inner: ScheduledNearMissPlanStore,
    var cache: TtlCache<ScheduledNearMissPlan>,
    var hits: Int,
    var misses: Int,
) : ScheduledNearMissPlanStore {
    override fun find(code: String): ScheduledNearMissPlan? {
        val hit = (cache.get(code) as ScheduledNearMissPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNearMissPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNearMissPlan> = inner.list()
}

fun newCachedScheduledNearMissPlanStore(inner: ScheduledNearMissPlanStore, ttl: Int, clock: Clock): ScheduledNearMissPlanStore {
    return CachedScheduledNearMissPlanStore(
        inner = inner,
        cache = newTtlCache<ScheduledNearMissPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled near miss plans. Empty fields match all. */
data class ScheduledNearMissPlanQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNearMissPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledNearMissPlan>): MutableList<ScheduledNearMissPlan> {
        val matched = filter(items) { candidate: ScheduledNearMissPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNearMissPlans(store: ScheduledNearMissPlanStore, query: ScheduledNearMissPlanQuery): MutableList<ScheduledNearMissPlan> {
    return query.apply(store.list())
}
