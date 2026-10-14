package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict appointment entries by code. */
interface StrictAppointmentEntryStore {
    fun find(code: String): StrictAppointmentEntry?
    fun save(item: StrictAppointmentEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAppointmentEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAppointmentEntryStore(
    var items: MutableMap<String, StrictAppointmentEntry>,
    var order: MutableList<String>,
) : StrictAppointmentEntryStore {
    override fun find(code: String): StrictAppointmentEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAppointmentEntry) {
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

    override fun list(): MutableList<StrictAppointmentEntry> {
        val out = mutableListOf<StrictAppointmentEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAppointmentEntryStore(): StrictAppointmentEntryStore {
    return MemoryStrictAppointmentEntryStore(
        mutableMapOf<String, StrictAppointmentEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAppointmentEntryStore(
    var inner: StrictAppointmentEntryStore,
    var cache: TtlCache<StrictAppointmentEntry>,
    var hits: Int,
    var misses: Int,
) : StrictAppointmentEntryStore {
    override fun find(code: String): StrictAppointmentEntry? {
        val hit = (cache.get(code) as StrictAppointmentEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAppointmentEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAppointmentEntry> = inner.list()
}

fun newCachedStrictAppointmentEntryStore(inner: StrictAppointmentEntryStore, ttl: Int, clock: Clock): StrictAppointmentEntryStore {
    return CachedStrictAppointmentEntryStore(
        inner = inner,
        cache = newTtlCache<StrictAppointmentEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict appointment entries. Empty fields match all. */
data class StrictAppointmentEntryQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAppointmentEntry): Boolean {
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

    fun apply(items: MutableList<StrictAppointmentEntry>): MutableList<StrictAppointmentEntry> {
        val matched = filter(items) { candidate: StrictAppointmentEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAppointmentEntries(store: StrictAppointmentEntryStore, query: StrictAppointmentEntryQuery): MutableList<StrictAppointmentEntry> {
    return query.apply(store.list())
}
