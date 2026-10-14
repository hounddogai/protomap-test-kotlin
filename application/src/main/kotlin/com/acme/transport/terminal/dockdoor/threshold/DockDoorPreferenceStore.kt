package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door preferences by code. */
interface DockDoorPreferenceStore {
    fun find(code: String): DockDoorPreference?
    fun save(item: DockDoorPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorPreferenceStore(
    var items: MutableMap<String, DockDoorPreference>,
    var order: MutableList<String>,
) : DockDoorPreferenceStore {
    override fun find(code: String): DockDoorPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorPreference) {
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

    override fun list(): MutableList<DockDoorPreference> {
        val out = mutableListOf<DockDoorPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorPreferenceStore(): DockDoorPreferenceStore {
    return MemoryDockDoorPreferenceStore(mutableMapOf<String, DockDoorPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorPreferenceStore(
    var inner: DockDoorPreferenceStore,
    var cache: TtlCache<DockDoorPreference>,
    var hits: Int,
    var misses: Int,
) : DockDoorPreferenceStore {
    override fun find(code: String): DockDoorPreference? {
        val hit = (cache.get(code) as DockDoorPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorPreference> = inner.list()
}

fun newCachedDockDoorPreferenceStore(inner: DockDoorPreferenceStore, ttl: Int, clock: Clock): DockDoorPreferenceStore {
    return CachedDockDoorPreferenceStore(
        inner = inner,
        cache = newTtlCache<DockDoorPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door preferences. Empty fields match all. */
data class DockDoorPreferenceQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorPreference>): MutableList<DockDoorPreference> {
        val matched = filter(items) { candidate: DockDoorPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorPreferences(store: DockDoorPreferenceStore, query: DockDoorPreferenceQuery): MutableList<DockDoorPreference> {
    return query.apply(store.list())
}
