package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy near miss plans by code. */
interface LegacyNearMissPlanStore {
    fun find(code: String): LegacyNearMissPlan?
    fun save(item: LegacyNearMissPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyNearMissPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyNearMissPlanStore(
    var items: MutableMap<String, LegacyNearMissPlan>,
    var order: MutableList<String>,
) : LegacyNearMissPlanStore {
    override fun find(code: String): LegacyNearMissPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyNearMissPlan) {
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

    override fun list(): MutableList<LegacyNearMissPlan> {
        val out = mutableListOf<LegacyNearMissPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyNearMissPlanStore(): LegacyNearMissPlanStore {
    return MemoryLegacyNearMissPlanStore(mutableMapOf<String, LegacyNearMissPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyNearMissPlanStore(
    var inner: LegacyNearMissPlanStore,
    var cache: TtlCache<LegacyNearMissPlan>,
    var hits: Int,
    var misses: Int,
) : LegacyNearMissPlanStore {
    override fun find(code: String): LegacyNearMissPlan? {
        val hit = (cache.get(code) as LegacyNearMissPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyNearMissPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyNearMissPlan> = inner.list()
}

fun newCachedLegacyNearMissPlanStore(inner: LegacyNearMissPlanStore, ttl: Int, clock: Clock): LegacyNearMissPlanStore {
    return CachedLegacyNearMissPlanStore(
        inner = inner,
        cache = newTtlCache<LegacyNearMissPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy near miss plans. Empty fields match all. */
data class LegacyNearMissPlanQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyNearMissPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LegacyNearMissPlan>): MutableList<LegacyNearMissPlan> {
        val matched = filter(items) { candidate: LegacyNearMissPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyNearMissPlans(store: LegacyNearMissPlanStore, query: LegacyNearMissPlanQuery): MutableList<LegacyNearMissPlan> {
    return query.apply(store.list())
}
