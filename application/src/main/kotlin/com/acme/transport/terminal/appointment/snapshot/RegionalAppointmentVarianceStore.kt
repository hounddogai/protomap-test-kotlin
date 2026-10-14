package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional appointment variances by code. */
interface RegionalAppointmentVarianceStore {
    fun find(code: String): RegionalAppointmentVariance?
    fun save(item: RegionalAppointmentVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAppointmentVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAppointmentVarianceStore(
    var items: MutableMap<String, RegionalAppointmentVariance>,
    var order: MutableList<String>,
) : RegionalAppointmentVarianceStore {
    override fun find(code: String): RegionalAppointmentVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAppointmentVariance) {
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

    override fun list(): MutableList<RegionalAppointmentVariance> {
        val out = mutableListOf<RegionalAppointmentVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAppointmentVarianceStore(): RegionalAppointmentVarianceStore {
    return MemoryRegionalAppointmentVarianceStore(
        mutableMapOf<String, RegionalAppointmentVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAppointmentVarianceStore(
    var inner: RegionalAppointmentVarianceStore,
    var cache: TtlCache<RegionalAppointmentVariance>,
    var hits: Int,
    var misses: Int,
) : RegionalAppointmentVarianceStore {
    override fun find(code: String): RegionalAppointmentVariance? {
        val hit = (cache.get(code) as RegionalAppointmentVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAppointmentVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAppointmentVariance> = inner.list()
}

fun newCachedRegionalAppointmentVarianceStore(inner: RegionalAppointmentVarianceStore, ttl: Int, clock: Clock): RegionalAppointmentVarianceStore {
    return CachedRegionalAppointmentVarianceStore(
        inner = inner,
        cache = newTtlCache<RegionalAppointmentVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional appointment variances. Empty fields match all. */
data class RegionalAppointmentVarianceQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAppointmentVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAppointmentVariance>): MutableList<RegionalAppointmentVariance> {
        val matched = filter(items) { candidate: RegionalAppointmentVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAppointmentVariances(store: RegionalAppointmentVarianceStore, query: RegionalAppointmentVarianceQuery): MutableList<RegionalAppointmentVariance> {
    return query.apply(store.list())
}
