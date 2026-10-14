package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment mappings by code. */
interface AppointmentMappingStore {
    fun find(code: String): AppointmentMapping?
    fun save(item: AppointmentMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentMappingStore(
    var items: MutableMap<String, AppointmentMapping>,
    var order: MutableList<String>,
) : AppointmentMappingStore {
    override fun find(code: String): AppointmentMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentMapping) {
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

    override fun list(): MutableList<AppointmentMapping> {
        val out = mutableListOf<AppointmentMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentMappingStore(): AppointmentMappingStore {
    return MemoryAppointmentMappingStore(mutableMapOf<String, AppointmentMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentMappingStore(
    var inner: AppointmentMappingStore,
    var cache: TtlCache<AppointmentMapping>,
    var hits: Int,
    var misses: Int,
) : AppointmentMappingStore {
    override fun find(code: String): AppointmentMapping? {
        val hit = (cache.get(code) as AppointmentMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentMapping> = inner.list()
}

fun newCachedAppointmentMappingStore(inner: AppointmentMappingStore, ttl: Int, clock: Clock): AppointmentMappingStore {
    return CachedAppointmentMappingStore(
        inner = inner,
        cache = newTtlCache<AppointmentMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment mappings. Empty fields match all. */
data class AppointmentMappingQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentMapping>): MutableList<AppointmentMapping> {
        val matched = filter(items) { candidate: AppointmentMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentMappings(store: AppointmentMappingStore, query: AppointmentMappingQuery): MutableList<AppointmentMapping> {
    return query.apply(store.list())
}
