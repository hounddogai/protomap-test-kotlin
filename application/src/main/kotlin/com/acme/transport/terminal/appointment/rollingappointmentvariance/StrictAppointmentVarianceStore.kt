package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict appointment variances by code. */
interface StrictAppointmentVarianceStore {
    fun find(code: String): StrictAppointmentVariance?
    fun save(item: StrictAppointmentVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAppointmentVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAppointmentVarianceStore(
    var items: MutableMap<String, StrictAppointmentVariance>,
    var order: MutableList<String>,
) : StrictAppointmentVarianceStore {
    override fun find(code: String): StrictAppointmentVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAppointmentVariance) {
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

    override fun list(): MutableList<StrictAppointmentVariance> {
        val out = mutableListOf<StrictAppointmentVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAppointmentVarianceStore(): StrictAppointmentVarianceStore {
    return MemoryStrictAppointmentVarianceStore(
        mutableMapOf<String, StrictAppointmentVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAppointmentVarianceStore(
    var inner: StrictAppointmentVarianceStore,
    var cache: TtlCache<StrictAppointmentVariance>,
    var hits: Int,
    var misses: Int,
) : StrictAppointmentVarianceStore {
    override fun find(code: String): StrictAppointmentVariance? {
        val hit = (cache.get(code) as StrictAppointmentVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAppointmentVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAppointmentVariance> = inner.list()
}

fun newCachedStrictAppointmentVarianceStore(inner: StrictAppointmentVarianceStore, ttl: Int, clock: Clock): StrictAppointmentVarianceStore {
    return CachedStrictAppointmentVarianceStore(
        inner = inner,
        cache = newTtlCache<StrictAppointmentVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict appointment variances. Empty fields match all. */
data class StrictAppointmentVarianceQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAppointmentVariance): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAppointmentVariance>): MutableList<StrictAppointmentVariance> {
        val matched = filter(items) { candidate: StrictAppointmentVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAppointmentVariances(store: StrictAppointmentVarianceStore, query: StrictAppointmentVarianceQuery): MutableList<StrictAppointmentVariance> {
    return query.apply(store.list())
}
