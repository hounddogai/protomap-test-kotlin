package com.acme.transport.terminal.appointment.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual appointment windows by code. */
interface ManualAppointmentWindowStore {
    fun find(code: String): ManualAppointmentWindow?
    fun save(item: ManualAppointmentWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAppointmentWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAppointmentWindowStore(
    var items: MutableMap<String, ManualAppointmentWindow>,
    var order: MutableList<String>,
) : ManualAppointmentWindowStore {
    override fun find(code: String): ManualAppointmentWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAppointmentWindow) {
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

    override fun list(): MutableList<ManualAppointmentWindow> {
        val out = mutableListOf<ManualAppointmentWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAppointmentWindowStore(): ManualAppointmentWindowStore {
    return MemoryManualAppointmentWindowStore(
        mutableMapOf<String, ManualAppointmentWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAppointmentWindowStore(
    var inner: ManualAppointmentWindowStore,
    var cache: TtlCache<ManualAppointmentWindow>,
    var hits: Int,
    var misses: Int,
) : ManualAppointmentWindowStore {
    override fun find(code: String): ManualAppointmentWindow? {
        val hit = (cache.get(code) as ManualAppointmentWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAppointmentWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAppointmentWindow> = inner.list()
}

fun newCachedManualAppointmentWindowStore(inner: ManualAppointmentWindowStore, ttl: Int, clock: Clock): ManualAppointmentWindowStore {
    return CachedManualAppointmentWindowStore(
        inner = inner,
        cache = newTtlCache<ManualAppointmentWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual appointment windows. Empty fields match all. */
data class ManualAppointmentWindowQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAppointmentWindow): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAppointmentWindow>): MutableList<ManualAppointmentWindow> {
        val matched = filter(items) { candidate: ManualAppointmentWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAppointmentWindows(store: ManualAppointmentWindowStore, query: ManualAppointmentWindowQuery): MutableList<ManualAppointmentWindow> {
    return query.apply(store.list())
}
