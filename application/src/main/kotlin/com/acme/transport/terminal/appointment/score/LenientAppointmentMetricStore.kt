package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient appointment metrics by code. */
interface LenientAppointmentMetricStore {
    fun find(code: String): LenientAppointmentMetric?
    fun save(item: LenientAppointmentMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAppointmentMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAppointmentMetricStore(
    var items: MutableMap<String, LenientAppointmentMetric>,
    var order: MutableList<String>,
) : LenientAppointmentMetricStore {
    override fun find(code: String): LenientAppointmentMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAppointmentMetric) {
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

    override fun list(): MutableList<LenientAppointmentMetric> {
        val out = mutableListOf<LenientAppointmentMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAppointmentMetricStore(): LenientAppointmentMetricStore {
    return MemoryLenientAppointmentMetricStore(
        mutableMapOf<String, LenientAppointmentMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAppointmentMetricStore(
    var inner: LenientAppointmentMetricStore,
    var cache: TtlCache<LenientAppointmentMetric>,
    var hits: Int,
    var misses: Int,
) : LenientAppointmentMetricStore {
    override fun find(code: String): LenientAppointmentMetric? {
        val hit = (cache.get(code) as LenientAppointmentMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAppointmentMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAppointmentMetric> = inner.list()
}

fun newCachedLenientAppointmentMetricStore(inner: LenientAppointmentMetricStore, ttl: Int, clock: Clock): LenientAppointmentMetricStore {
    return CachedLenientAppointmentMetricStore(
        inner = inner,
        cache = newTtlCache<LenientAppointmentMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient appointment metrics. Empty fields match all. */
data class LenientAppointmentMetricQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAppointmentMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAppointmentMetric>): MutableList<LenientAppointmentMetric> {
        val matched = filter(items) { candidate: LenientAppointmentMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAppointmentMetrics(store: LenientAppointmentMetricStore, query: LenientAppointmentMetricQuery): MutableList<LenientAppointmentMetric> {
    return query.apply(store.list())
}
