package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback appointment snapshots by code. */
interface FallbackAppointmentSnapshotStore {
    fun find(code: String): FallbackAppointmentSnapshot?
    fun save(item: FallbackAppointmentSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAppointmentSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAppointmentSnapshotStore(
    var items: MutableMap<String, FallbackAppointmentSnapshot>,
    var order: MutableList<String>,
) : FallbackAppointmentSnapshotStore {
    override fun find(code: String): FallbackAppointmentSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAppointmentSnapshot) {
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

    override fun list(): MutableList<FallbackAppointmentSnapshot> {
        val out = mutableListOf<FallbackAppointmentSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAppointmentSnapshotStore(): FallbackAppointmentSnapshotStore {
    return MemoryFallbackAppointmentSnapshotStore(
        mutableMapOf<String, FallbackAppointmentSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAppointmentSnapshotStore(
    var inner: FallbackAppointmentSnapshotStore,
    var cache: TtlCache<FallbackAppointmentSnapshot>,
    var hits: Int,
    var misses: Int,
) : FallbackAppointmentSnapshotStore {
    override fun find(code: String): FallbackAppointmentSnapshot? {
        val hit = (cache.get(code) as FallbackAppointmentSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAppointmentSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAppointmentSnapshot> = inner.list()
}

fun newCachedFallbackAppointmentSnapshotStore(inner: FallbackAppointmentSnapshotStore, ttl: Int, clock: Clock): FallbackAppointmentSnapshotStore {
    return CachedFallbackAppointmentSnapshotStore(
        inner = inner,
        cache = newTtlCache<FallbackAppointmentSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback appointment snapshots. Empty fields match all. */
data class FallbackAppointmentSnapshotQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAppointmentSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAppointmentSnapshot>): MutableList<FallbackAppointmentSnapshot> {
        val matched = filter(items) { candidate: FallbackAppointmentSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAppointmentSnapshots(store: FallbackAppointmentSnapshotStore, query: FallbackAppointmentSnapshotQuery): MutableList<FallbackAppointmentSnapshot> {
    return query.apply(store.list())
}
