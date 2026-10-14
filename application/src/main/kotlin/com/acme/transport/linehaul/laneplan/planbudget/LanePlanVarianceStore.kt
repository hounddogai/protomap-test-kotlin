package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan variances by code. */
interface LanePlanVarianceStore {
    fun find(code: String): LanePlanVariance?
    fun save(item: LanePlanVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanVarianceStore(
    var items: MutableMap<String, LanePlanVariance>,
    var order: MutableList<String>,
) : LanePlanVarianceStore {
    override fun find(code: String): LanePlanVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanVariance) {
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

    override fun list(): MutableList<LanePlanVariance> {
        val out = mutableListOf<LanePlanVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanVarianceStore(): LanePlanVarianceStore {
    return MemoryLanePlanVarianceStore(mutableMapOf<String, LanePlanVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanVarianceStore(
    var inner: LanePlanVarianceStore,
    var cache: TtlCache<LanePlanVariance>,
    var hits: Int,
    var misses: Int,
) : LanePlanVarianceStore {
    override fun find(code: String): LanePlanVariance? {
        val hit = (cache.get(code) as LanePlanVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanVariance> = inner.list()
}

fun newCachedLanePlanVarianceStore(inner: LanePlanVarianceStore, ttl: Int, clock: Clock): LanePlanVarianceStore {
    return CachedLanePlanVarianceStore(
        inner = inner,
        cache = newTtlCache<LanePlanVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan variances. Empty fields match all. */
data class LanePlanVarianceQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<LanePlanVariance>): MutableList<LanePlanVariance> {
        val matched = filter(items) { candidate: LanePlanVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanVariances(store: LanePlanVarianceStore, query: LanePlanVarianceQuery): MutableList<LanePlanVariance> {
    return query.apply(store.list())
}
