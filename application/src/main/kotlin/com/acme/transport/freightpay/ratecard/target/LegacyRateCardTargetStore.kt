package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rate card targets by code. */
interface LegacyRateCardTargetStore {
    fun find(code: String): LegacyRateCardTarget?
    fun save(item: LegacyRateCardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRateCardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRateCardTargetStore(
    var items: MutableMap<String, LegacyRateCardTarget>,
    var order: MutableList<String>,
) : LegacyRateCardTargetStore {
    override fun find(code: String): LegacyRateCardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRateCardTarget) {
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

    override fun list(): MutableList<LegacyRateCardTarget> {
        val out = mutableListOf<LegacyRateCardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRateCardTargetStore(): LegacyRateCardTargetStore {
    return MemoryLegacyRateCardTargetStore(
        mutableMapOf<String, LegacyRateCardTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRateCardTargetStore(
    var inner: LegacyRateCardTargetStore,
    var cache: TtlCache<LegacyRateCardTarget>,
    var hits: Int,
    var misses: Int,
) : LegacyRateCardTargetStore {
    override fun find(code: String): LegacyRateCardTarget? {
        val hit = (cache.get(code) as LegacyRateCardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRateCardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRateCardTarget> = inner.list()
}

fun newCachedLegacyRateCardTargetStore(inner: LegacyRateCardTargetStore, ttl: Int, clock: Clock): LegacyRateCardTargetStore {
    return CachedLegacyRateCardTargetStore(
        inner = inner,
        cache = newTtlCache<LegacyRateCardTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rate card targets. Empty fields match all. */
data class LegacyRateCardTargetQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRateCardTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LegacyRateCardTarget>): MutableList<LegacyRateCardTarget> {
        val matched = filter(items) { candidate: LegacyRateCardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRateCardTargets(store: LegacyRateCardTargetStore, query: LegacyRateCardTargetQuery): MutableList<LegacyRateCardTarget> {
    return query.apply(store.list())
}
