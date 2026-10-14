package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock preferences by code. */
interface CrossDockPreferenceStore {
    fun find(code: String): CrossDockPreference?
    fun save(item: CrossDockPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockPreferenceStore(
    var items: MutableMap<String, CrossDockPreference>,
    var order: MutableList<String>,
) : CrossDockPreferenceStore {
    override fun find(code: String): CrossDockPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockPreference) {
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

    override fun list(): MutableList<CrossDockPreference> {
        val out = mutableListOf<CrossDockPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockPreferenceStore(): CrossDockPreferenceStore {
    return MemoryCrossDockPreferenceStore(mutableMapOf<String, CrossDockPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockPreferenceStore(
    var inner: CrossDockPreferenceStore,
    var cache: TtlCache<CrossDockPreference>,
    var hits: Int,
    var misses: Int,
) : CrossDockPreferenceStore {
    override fun find(code: String): CrossDockPreference? {
        val hit = (cache.get(code) as CrossDockPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockPreference> = inner.list()
}

fun newCachedCrossDockPreferenceStore(inner: CrossDockPreferenceStore, ttl: Int, clock: Clock): CrossDockPreferenceStore {
    return CachedCrossDockPreferenceStore(
        inner = inner,
        cache = newTtlCache<CrossDockPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock preferences. Empty fields match all. */
data class CrossDockPreferenceQuery(
    var segment: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockPreference): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockPreference>): MutableList<CrossDockPreference> {
        val matched = filter(items) { candidate: CrossDockPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockPreferences(store: CrossDockPreferenceStore, query: CrossDockPreferenceQuery): MutableList<CrossDockPreference> {
    return query.apply(store.list())
}
