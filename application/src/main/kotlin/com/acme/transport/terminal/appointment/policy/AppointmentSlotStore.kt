package com.acme.transport.terminal.appointment.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment slots by code. */
interface AppointmentSlotStore {
    fun find(code: String): AppointmentSlot?
    fun save(item: AppointmentSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentSlotStore(
    var items: MutableMap<String, AppointmentSlot>,
    var order: MutableList<String>,
) : AppointmentSlotStore {
    override fun find(code: String): AppointmentSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentSlot) {
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

    override fun list(): MutableList<AppointmentSlot> {
        val out = mutableListOf<AppointmentSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentSlotStore(): AppointmentSlotStore {
    return MemoryAppointmentSlotStore(mutableMapOf<String, AppointmentSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentSlotStore(
    var inner: AppointmentSlotStore,
    var cache: TtlCache<AppointmentSlot>,
    var hits: Int,
    var misses: Int,
) : AppointmentSlotStore {
    override fun find(code: String): AppointmentSlot? {
        val hit = (cache.get(code) as AppointmentSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentSlot> = inner.list()
}

fun newCachedAppointmentSlotStore(inner: AppointmentSlotStore, ttl: Int, clock: Clock): AppointmentSlotStore {
    return CachedAppointmentSlotStore(
        inner = inner,
        cache = newTtlCache<AppointmentSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment slots. Empty fields match all. */
data class AppointmentSlotQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AppointmentSlot>): MutableList<AppointmentSlot> {
        val matched = filter(items) { candidate: AppointmentSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentSlots(store: AppointmentSlotStore, query: AppointmentSlotQuery): MutableList<AppointmentSlot> {
    return query.apply(store.list())
}
