package com.acme.transport.terminal.appointment.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback appointment entries by code. */
interface FallbackAppointmentEntryStore {
    fun find(code: String): FallbackAppointmentEntry?
    fun save(item: FallbackAppointmentEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAppointmentEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAppointmentEntryStore(
    var items: MutableMap<String, FallbackAppointmentEntry>,
    var order: MutableList<String>,
) : FallbackAppointmentEntryStore {
    override fun find(code: String): FallbackAppointmentEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAppointmentEntry) {
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

    override fun list(): MutableList<FallbackAppointmentEntry> {
        val out = mutableListOf<FallbackAppointmentEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAppointmentEntryStore(): FallbackAppointmentEntryStore {
    return MemoryFallbackAppointmentEntryStore(
        mutableMapOf<String, FallbackAppointmentEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAppointmentEntryStore(
    var inner: FallbackAppointmentEntryStore,
    var cache: TtlCache<FallbackAppointmentEntry>,
    var hits: Int,
    var misses: Int,
) : FallbackAppointmentEntryStore {
    override fun find(code: String): FallbackAppointmentEntry? {
        val hit = (cache.get(code) as FallbackAppointmentEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAppointmentEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAppointmentEntry> = inner.list()
}

fun newCachedFallbackAppointmentEntryStore(inner: FallbackAppointmentEntryStore, ttl: Int, clock: Clock): FallbackAppointmentEntryStore {
    return CachedFallbackAppointmentEntryStore(
        inner = inner,
        cache = newTtlCache<FallbackAppointmentEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback appointment entries. Empty fields match all. */
data class FallbackAppointmentEntryQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAppointmentEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAppointmentEntry>): MutableList<FallbackAppointmentEntry> {
        val matched = filter(items) { candidate: FallbackAppointmentEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAppointmentEntries(store: FallbackAppointmentEntryStore, query: FallbackAppointmentEntryQuery): MutableList<FallbackAppointmentEntry> {
    return query.apply(store.list())
}
