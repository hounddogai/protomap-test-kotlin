package com.acme.transport.safety.incident.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident windows by code. */
interface IncidentWindowStore {
    fun find(code: String): IncidentWindow?
    fun save(item: IncidentWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentWindowStore(
    var items: MutableMap<String, IncidentWindow>,
    var order: MutableList<String>,
) : IncidentWindowStore {
    override fun find(code: String): IncidentWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentWindow) {
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

    override fun list(): MutableList<IncidentWindow> {
        val out = mutableListOf<IncidentWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentWindowStore(): IncidentWindowStore {
    return MemoryIncidentWindowStore(mutableMapOf<String, IncidentWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentWindowStore(
    var inner: IncidentWindowStore,
    var cache: TtlCache<IncidentWindow>,
    var hits: Int,
    var misses: Int,
) : IncidentWindowStore {
    override fun find(code: String): IncidentWindow? {
        val hit = (cache.get(code) as IncidentWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentWindow> = inner.list()
}

fun newCachedIncidentWindowStore(inner: IncidentWindowStore, ttl: Int, clock: Clock): IncidentWindowStore {
    return CachedIncidentWindowStore(
        inner = inner,
        cache = newTtlCache<IncidentWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident windows. Empty fields match all. */
data class IncidentWindowQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentWindow>): MutableList<IncidentWindow> {
        val matched = filter(items) { candidate: IncidentWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentWindows(store: IncidentWindowStore, query: IncidentWindowQuery): MutableList<IncidentWindow> {
    return query.apply(store.list())
}
