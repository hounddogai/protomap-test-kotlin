package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dwell preferences by code. */
interface FallbackDwellPreferenceStore {
    fun find(code: String): FallbackDwellPreference?
    fun save(item: FallbackDwellPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDwellPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDwellPreferenceStore(
    var items: MutableMap<String, FallbackDwellPreference>,
    var order: MutableList<String>,
) : FallbackDwellPreferenceStore {
    override fun find(code: String): FallbackDwellPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDwellPreference) {
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

    override fun list(): MutableList<FallbackDwellPreference> {
        val out = mutableListOf<FallbackDwellPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDwellPreferenceStore(): FallbackDwellPreferenceStore {
    return MemoryFallbackDwellPreferenceStore(
        mutableMapOf<String, FallbackDwellPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDwellPreferenceStore(
    var inner: FallbackDwellPreferenceStore,
    var cache: TtlCache<FallbackDwellPreference>,
    var hits: Int,
    var misses: Int,
) : FallbackDwellPreferenceStore {
    override fun find(code: String): FallbackDwellPreference? {
        val hit = (cache.get(code) as FallbackDwellPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDwellPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDwellPreference> = inner.list()
}

fun newCachedFallbackDwellPreferenceStore(inner: FallbackDwellPreferenceStore, ttl: Int, clock: Clock): FallbackDwellPreferenceStore {
    return CachedFallbackDwellPreferenceStore(
        inner = inner,
        cache = newTtlCache<FallbackDwellPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dwell preferences. Empty fields match all. */
data class FallbackDwellPreferenceQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDwellPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDwellPreference>): MutableList<FallbackDwellPreference> {
        val matched = filter(items) { candidate: FallbackDwellPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDwellPreferences(store: FallbackDwellPreferenceStore, query: FallbackDwellPreferenceQuery): MutableList<FallbackDwellPreference> {
    return query.apply(store.list())
}
