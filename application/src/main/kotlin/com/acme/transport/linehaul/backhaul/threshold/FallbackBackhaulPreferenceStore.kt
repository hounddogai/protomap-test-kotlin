package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback backhaul preferences by code. */
interface FallbackBackhaulPreferenceStore {
    fun find(code: String): FallbackBackhaulPreference?
    fun save(item: FallbackBackhaulPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackBackhaulPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackBackhaulPreferenceStore(
    var items: MutableMap<String, FallbackBackhaulPreference>,
    var order: MutableList<String>,
) : FallbackBackhaulPreferenceStore {
    override fun find(code: String): FallbackBackhaulPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackBackhaulPreference) {
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

    override fun list(): MutableList<FallbackBackhaulPreference> {
        val out = mutableListOf<FallbackBackhaulPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackBackhaulPreferenceStore(): FallbackBackhaulPreferenceStore {
    return MemoryFallbackBackhaulPreferenceStore(
        mutableMapOf<String, FallbackBackhaulPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackBackhaulPreferenceStore(
    var inner: FallbackBackhaulPreferenceStore,
    var cache: TtlCache<FallbackBackhaulPreference>,
    var hits: Int,
    var misses: Int,
) : FallbackBackhaulPreferenceStore {
    override fun find(code: String): FallbackBackhaulPreference? {
        val hit = (cache.get(code) as FallbackBackhaulPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackBackhaulPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackBackhaulPreference> = inner.list()
}

fun newCachedFallbackBackhaulPreferenceStore(inner: FallbackBackhaulPreferenceStore, ttl: Int, clock: Clock): FallbackBackhaulPreferenceStore {
    return CachedFallbackBackhaulPreferenceStore(
        inner = inner,
        cache = newTtlCache<FallbackBackhaulPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback backhaul preferences. Empty fields match all. */
data class FallbackBackhaulPreferenceQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackBackhaulPreference): Boolean {
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

    fun apply(items: MutableList<FallbackBackhaulPreference>): MutableList<FallbackBackhaulPreference> {
        val matched = filter(items) { candidate: FallbackBackhaulPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackBackhaulPreferences(store: FallbackBackhaulPreferenceStore, query: FallbackBackhaulPreferenceQuery): MutableList<FallbackBackhaulPreference> {
    return query.apply(store.list())
}
