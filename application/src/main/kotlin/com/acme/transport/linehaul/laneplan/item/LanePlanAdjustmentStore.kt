package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan adjustments by code. */
interface LanePlanAdjustmentStore {
    fun find(code: String): LanePlanAdjustment?
    fun save(item: LanePlanAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanAdjustmentStore(
    var items: MutableMap<String, LanePlanAdjustment>,
    var order: MutableList<String>,
) : LanePlanAdjustmentStore {
    override fun find(code: String): LanePlanAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanAdjustment) {
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

    override fun list(): MutableList<LanePlanAdjustment> {
        val out = mutableListOf<LanePlanAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanAdjustmentStore(): LanePlanAdjustmentStore {
    return MemoryLanePlanAdjustmentStore(mutableMapOf<String, LanePlanAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanAdjustmentStore(
    var inner: LanePlanAdjustmentStore,
    var cache: TtlCache<LanePlanAdjustment>,
    var hits: Int,
    var misses: Int,
) : LanePlanAdjustmentStore {
    override fun find(code: String): LanePlanAdjustment? {
        val hit = (cache.get(code) as LanePlanAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanAdjustment> = inner.list()
}

fun newCachedLanePlanAdjustmentStore(inner: LanePlanAdjustmentStore, ttl: Int, clock: Clock): LanePlanAdjustmentStore {
    return CachedLanePlanAdjustmentStore(
        inner = inner,
        cache = newTtlCache<LanePlanAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan adjustments. Empty fields match all. */
data class LanePlanAdjustmentQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LanePlanAdjustment>): MutableList<LanePlanAdjustment> {
        val matched = filter(items) { candidate: LanePlanAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanAdjustments(store: LanePlanAdjustmentStore, query: LanePlanAdjustmentQuery): MutableList<LanePlanAdjustment> {
    return query.apply(store.list())
}
