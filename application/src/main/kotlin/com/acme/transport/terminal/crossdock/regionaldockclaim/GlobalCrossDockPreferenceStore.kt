package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global cross dock preferences by code. */
interface GlobalCrossDockPreferenceStore {
    fun find(code: String): GlobalCrossDockPreference?
    fun save(item: GlobalCrossDockPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCrossDockPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCrossDockPreferenceStore(
    var items: MutableMap<String, GlobalCrossDockPreference>,
    var order: MutableList<String>,
) : GlobalCrossDockPreferenceStore {
    override fun find(code: String): GlobalCrossDockPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCrossDockPreference) {
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

    override fun list(): MutableList<GlobalCrossDockPreference> {
        val out = mutableListOf<GlobalCrossDockPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCrossDockPreferenceStore(): GlobalCrossDockPreferenceStore {
    return MemoryGlobalCrossDockPreferenceStore(
        mutableMapOf<String, GlobalCrossDockPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCrossDockPreferenceStore(
    var inner: GlobalCrossDockPreferenceStore,
    var cache: TtlCache<GlobalCrossDockPreference>,
    var hits: Int,
    var misses: Int,
) : GlobalCrossDockPreferenceStore {
    override fun find(code: String): GlobalCrossDockPreference? {
        val hit = (cache.get(code) as GlobalCrossDockPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCrossDockPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCrossDockPreference> = inner.list()
}

fun newCachedGlobalCrossDockPreferenceStore(inner: GlobalCrossDockPreferenceStore, ttl: Int, clock: Clock): GlobalCrossDockPreferenceStore {
    return CachedGlobalCrossDockPreferenceStore(
        inner = inner,
        cache = newTtlCache<GlobalCrossDockPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cross dock preferences. Empty fields match all. */
data class GlobalCrossDockPreferenceQuery(
    var country: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCrossDockPreference): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCrossDockPreference>): MutableList<GlobalCrossDockPreference> {
        val matched = filter(items) { candidate: GlobalCrossDockPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCrossDockPreferences(store: GlobalCrossDockPreferenceStore, query: GlobalCrossDockPreferenceQuery): MutableList<GlobalCrossDockPreference> {
    return query.apply(store.list())
}
