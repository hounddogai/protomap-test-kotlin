package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment thresholds by code. */
interface AppointmentThresholdStore {
    fun find(code: String): AppointmentThreshold?
    fun save(item: AppointmentThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentThresholdStore(
    var items: MutableMap<String, AppointmentThreshold>,
    var order: MutableList<String>,
) : AppointmentThresholdStore {
    override fun find(code: String): AppointmentThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentThreshold) {
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

    override fun list(): MutableList<AppointmentThreshold> {
        val out = mutableListOf<AppointmentThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentThresholdStore(): AppointmentThresholdStore {
    return MemoryAppointmentThresholdStore(
        mutableMapOf<String, AppointmentThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentThresholdStore(
    var inner: AppointmentThresholdStore,
    var cache: TtlCache<AppointmentThreshold>,
    var hits: Int,
    var misses: Int,
) : AppointmentThresholdStore {
    override fun find(code: String): AppointmentThreshold? {
        val hit = (cache.get(code) as AppointmentThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentThreshold> = inner.list()
}

fun newCachedAppointmentThresholdStore(inner: AppointmentThresholdStore, ttl: Int, clock: Clock): AppointmentThresholdStore {
    return CachedAppointmentThresholdStore(
        inner = inner,
        cache = newTtlCache<AppointmentThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment thresholds. Empty fields match all. */
data class AppointmentThresholdQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentThreshold): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<AppointmentThreshold>): MutableList<AppointmentThreshold> {
        val matched = filter(items) { candidate: AppointmentThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentThresholds(store: AppointmentThresholdStore, query: AppointmentThresholdQuery): MutableList<AppointmentThreshold> {
    return query.apply(store.list())
}
