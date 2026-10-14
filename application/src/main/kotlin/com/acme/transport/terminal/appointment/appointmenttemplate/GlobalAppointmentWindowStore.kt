package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global appointment windows by code. */
interface GlobalAppointmentWindowStore {
    fun find(code: String): GlobalAppointmentWindow?
    fun save(item: GlobalAppointmentWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAppointmentWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAppointmentWindowStore(
    var items: MutableMap<String, GlobalAppointmentWindow>,
    var order: MutableList<String>,
) : GlobalAppointmentWindowStore {
    override fun find(code: String): GlobalAppointmentWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAppointmentWindow) {
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

    override fun list(): MutableList<GlobalAppointmentWindow> {
        val out = mutableListOf<GlobalAppointmentWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAppointmentWindowStore(): GlobalAppointmentWindowStore {
    return MemoryGlobalAppointmentWindowStore(
        mutableMapOf<String, GlobalAppointmentWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAppointmentWindowStore(
    var inner: GlobalAppointmentWindowStore,
    var cache: TtlCache<GlobalAppointmentWindow>,
    var hits: Int,
    var misses: Int,
) : GlobalAppointmentWindowStore {
    override fun find(code: String): GlobalAppointmentWindow? {
        val hit = (cache.get(code) as GlobalAppointmentWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAppointmentWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAppointmentWindow> = inner.list()
}

fun newCachedGlobalAppointmentWindowStore(inner: GlobalAppointmentWindowStore, ttl: Int, clock: Clock): GlobalAppointmentWindowStore {
    return CachedGlobalAppointmentWindowStore(
        inner = inner,
        cache = newTtlCache<GlobalAppointmentWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global appointment windows. Empty fields match all. */
data class GlobalAppointmentWindowQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAppointmentWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAppointmentWindow>): MutableList<GlobalAppointmentWindow> {
        val matched = filter(items) { candidate: GlobalAppointmentWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAppointmentWindows(store: GlobalAppointmentWindowStore, query: GlobalAppointmentWindowQuery): MutableList<GlobalAppointmentWindow> {
    return query.apply(store.list())
}
