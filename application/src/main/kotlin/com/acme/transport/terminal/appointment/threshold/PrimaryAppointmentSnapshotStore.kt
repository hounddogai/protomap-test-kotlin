package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary appointment snapshots by code. */
interface PrimaryAppointmentSnapshotStore {
    fun find(code: String): PrimaryAppointmentSnapshot?
    fun save(item: PrimaryAppointmentSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAppointmentSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAppointmentSnapshotStore(
    var items: MutableMap<String, PrimaryAppointmentSnapshot>,
    var order: MutableList<String>,
) : PrimaryAppointmentSnapshotStore {
    override fun find(code: String): PrimaryAppointmentSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAppointmentSnapshot) {
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

    override fun list(): MutableList<PrimaryAppointmentSnapshot> {
        val out = mutableListOf<PrimaryAppointmentSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAppointmentSnapshotStore(): PrimaryAppointmentSnapshotStore {
    return MemoryPrimaryAppointmentSnapshotStore(
        mutableMapOf<String, PrimaryAppointmentSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAppointmentSnapshotStore(
    var inner: PrimaryAppointmentSnapshotStore,
    var cache: TtlCache<PrimaryAppointmentSnapshot>,
    var hits: Int,
    var misses: Int,
) : PrimaryAppointmentSnapshotStore {
    override fun find(code: String): PrimaryAppointmentSnapshot? {
        val hit = (cache.get(code) as PrimaryAppointmentSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAppointmentSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAppointmentSnapshot> = inner.list()
}

fun newCachedPrimaryAppointmentSnapshotStore(inner: PrimaryAppointmentSnapshotStore, ttl: Int, clock: Clock): PrimaryAppointmentSnapshotStore {
    return CachedPrimaryAppointmentSnapshotStore(
        inner = inner,
        cache = newTtlCache<PrimaryAppointmentSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary appointment snapshots. Empty fields match all. */
data class PrimaryAppointmentSnapshotQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAppointmentSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAppointmentSnapshot>): MutableList<PrimaryAppointmentSnapshot> {
        val matched = filter(items) { candidate: PrimaryAppointmentSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAppointmentSnapshots(store: PrimaryAppointmentSnapshotStore, query: PrimaryAppointmentSnapshotQuery): MutableList<PrimaryAppointmentSnapshot> {
    return query.apply(store.list())
}
