package com.acme.transport.terminal.appointment.appointmentrollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment entries by code. */
interface AppointmentEntryStore {
    fun find(code: String): AppointmentEntry?
    fun save(item: AppointmentEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentEntryStore(
    var items: MutableMap<String, AppointmentEntry>,
    var order: MutableList<String>,
) : AppointmentEntryStore {
    override fun find(code: String): AppointmentEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentEntry) {
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

    override fun list(): MutableList<AppointmentEntry> {
        val out = mutableListOf<AppointmentEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentEntryStore(): AppointmentEntryStore {
    return MemoryAppointmentEntryStore(mutableMapOf<String, AppointmentEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentEntryStore(
    var inner: AppointmentEntryStore,
    var cache: TtlCache<AppointmentEntry>,
    var hits: Int,
    var misses: Int,
) : AppointmentEntryStore {
    override fun find(code: String): AppointmentEntry? {
        val hit = (cache.get(code) as AppointmentEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentEntry> = inner.list()
}

fun newCachedAppointmentEntryStore(inner: AppointmentEntryStore, ttl: Int, clock: Clock): AppointmentEntryStore {
    return CachedAppointmentEntryStore(
        inner = inner,
        cache = newTtlCache<AppointmentEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment entries. Empty fields match all. */
data class AppointmentEntryQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentEntry>): MutableList<AppointmentEntry> {
        val matched = filter(items) { candidate: AppointmentEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentEntries(store: AppointmentEntryStore, query: AppointmentEntryQuery): MutableList<AppointmentEntry> {
    return query.apply(store.list())
}
