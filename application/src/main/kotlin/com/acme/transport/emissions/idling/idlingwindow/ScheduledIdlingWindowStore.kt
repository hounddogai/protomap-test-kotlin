package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled idling windows by code. */
interface ScheduledIdlingWindowStore {
    fun find(code: String): ScheduledIdlingWindow?
    fun save(item: ScheduledIdlingWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledIdlingWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledIdlingWindowStore(
    var items: MutableMap<String, ScheduledIdlingWindow>,
    var order: MutableList<String>,
) : ScheduledIdlingWindowStore {
    override fun find(code: String): ScheduledIdlingWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledIdlingWindow) {
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

    override fun list(): MutableList<ScheduledIdlingWindow> {
        val out = mutableListOf<ScheduledIdlingWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledIdlingWindowStore(): ScheduledIdlingWindowStore {
    return MemoryScheduledIdlingWindowStore(
        mutableMapOf<String, ScheduledIdlingWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledIdlingWindowStore(
    var inner: ScheduledIdlingWindowStore,
    var cache: TtlCache<ScheduledIdlingWindow>,
    var hits: Int,
    var misses: Int,
) : ScheduledIdlingWindowStore {
    override fun find(code: String): ScheduledIdlingWindow? {
        val hit = (cache.get(code) as ScheduledIdlingWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledIdlingWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledIdlingWindow> = inner.list()
}

fun newCachedScheduledIdlingWindowStore(inner: ScheduledIdlingWindowStore, ttl: Int, clock: Clock): ScheduledIdlingWindowStore {
    return CachedScheduledIdlingWindowStore(
        inner = inner,
        cache = newTtlCache<ScheduledIdlingWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled idling windows. Empty fields match all. */
data class ScheduledIdlingWindowQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledIdlingWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledIdlingWindow>): MutableList<ScheduledIdlingWindow> {
        val matched = filter(items) { candidate: ScheduledIdlingWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledIdlingWindows(store: ScheduledIdlingWindowStore, query: ScheduledIdlingWindowQuery): MutableList<ScheduledIdlingWindow> {
    return query.apply(store.list())
}
