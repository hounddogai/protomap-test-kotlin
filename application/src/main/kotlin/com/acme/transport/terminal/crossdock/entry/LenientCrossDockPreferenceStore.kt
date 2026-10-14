package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient cross dock preferences by code. */
interface LenientCrossDockPreferenceStore {
    fun find(code: String): LenientCrossDockPreference?
    fun save(item: LenientCrossDockPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCrossDockPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCrossDockPreferenceStore(
    var items: MutableMap<String, LenientCrossDockPreference>,
    var order: MutableList<String>,
) : LenientCrossDockPreferenceStore {
    override fun find(code: String): LenientCrossDockPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCrossDockPreference) {
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

    override fun list(): MutableList<LenientCrossDockPreference> {
        val out = mutableListOf<LenientCrossDockPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCrossDockPreferenceStore(): LenientCrossDockPreferenceStore {
    return MemoryLenientCrossDockPreferenceStore(
        mutableMapOf<String, LenientCrossDockPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCrossDockPreferenceStore(
    var inner: LenientCrossDockPreferenceStore,
    var cache: TtlCache<LenientCrossDockPreference>,
    var hits: Int,
    var misses: Int,
) : LenientCrossDockPreferenceStore {
    override fun find(code: String): LenientCrossDockPreference? {
        val hit = (cache.get(code) as LenientCrossDockPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCrossDockPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCrossDockPreference> = inner.list()
}

fun newCachedLenientCrossDockPreferenceStore(inner: LenientCrossDockPreferenceStore, ttl: Int, clock: Clock): LenientCrossDockPreferenceStore {
    return CachedLenientCrossDockPreferenceStore(
        inner = inner,
        cache = newTtlCache<LenientCrossDockPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cross dock preferences. Empty fields match all. */
data class LenientCrossDockPreferenceQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCrossDockPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCrossDockPreference>): MutableList<LenientCrossDockPreference> {
        val matched = filter(items) { candidate: LenientCrossDockPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCrossDockPreferences(store: LenientCrossDockPreferenceStore, query: LenientCrossDockPreferenceQuery): MutableList<LenientCrossDockPreference> {
    return query.apply(store.list())
}
