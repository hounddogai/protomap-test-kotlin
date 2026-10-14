package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment allocations by code. */
interface AppointmentAllocationStore {
    fun find(code: String): AppointmentAllocation?
    fun save(item: AppointmentAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentAllocationStore(
    var items: MutableMap<String, AppointmentAllocation>,
    var order: MutableList<String>,
) : AppointmentAllocationStore {
    override fun find(code: String): AppointmentAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentAllocation) {
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

    override fun list(): MutableList<AppointmentAllocation> {
        val out = mutableListOf<AppointmentAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentAllocationStore(): AppointmentAllocationStore {
    return MemoryAppointmentAllocationStore(
        mutableMapOf<String, AppointmentAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentAllocationStore(
    var inner: AppointmentAllocationStore,
    var cache: TtlCache<AppointmentAllocation>,
    var hits: Int,
    var misses: Int,
) : AppointmentAllocationStore {
    override fun find(code: String): AppointmentAllocation? {
        val hit = (cache.get(code) as AppointmentAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentAllocation> = inner.list()
}

fun newCachedAppointmentAllocationStore(inner: AppointmentAllocationStore, ttl: Int, clock: Clock): AppointmentAllocationStore {
    return CachedAppointmentAllocationStore(
        inner = inner,
        cache = newTtlCache<AppointmentAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment allocations. Empty fields match all. */
data class AppointmentAllocationQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentAllocation>): MutableList<AppointmentAllocation> {
        val matched = filter(items) { candidate: AppointmentAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentAllocations(store: AppointmentAllocationStore, query: AppointmentAllocationQuery): MutableList<AppointmentAllocation> {
    return query.apply(store.list())
}
