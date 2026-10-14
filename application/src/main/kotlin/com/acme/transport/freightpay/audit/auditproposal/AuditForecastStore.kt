package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit forecasts by code. */
interface AuditForecastStore {
    fun find(code: String): AuditForecast?
    fun save(item: AuditForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditForecastStore(
    var items: MutableMap<String, AuditForecast>,
    var order: MutableList<String>,
) : AuditForecastStore {
    override fun find(code: String): AuditForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditForecast) {
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

    override fun list(): MutableList<AuditForecast> {
        val out = mutableListOf<AuditForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditForecastStore(): AuditForecastStore {
    return MemoryAuditForecastStore(mutableMapOf<String, AuditForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditForecastStore(
    var inner: AuditForecastStore,
    var cache: TtlCache<AuditForecast>,
    var hits: Int,
    var misses: Int,
) : AuditForecastStore {
    override fun find(code: String): AuditForecast? {
        val hit = (cache.get(code) as AuditForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditForecast> = inner.list()
}

fun newCachedAuditForecastStore(inner: AuditForecastStore, ttl: Int, clock: Clock): AuditForecastStore {
    return CachedAuditForecastStore(
        inner = inner,
        cache = newTtlCache<AuditForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit forecasts. Empty fields match all. */
data class AuditForecastQuery(
    var unit: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditForecast): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<AuditForecast>): MutableList<AuditForecast> {
        val matched = filter(items) { candidate: AuditForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditForecasts(store: AuditForecastStore, query: AuditForecastQuery): MutableList<AuditForecast> {
    return query.apply(store.list())
}
