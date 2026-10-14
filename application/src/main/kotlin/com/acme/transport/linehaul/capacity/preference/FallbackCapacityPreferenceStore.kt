package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback capacity preferences by code. */
interface FallbackCapacityPreferenceStore {
    fun find(code: String): FallbackCapacityPreference?
    fun save(item: FallbackCapacityPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCapacityPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCapacityPreferenceStore(
    var items: MutableMap<String, FallbackCapacityPreference>,
    var order: MutableList<String>,
) : FallbackCapacityPreferenceStore {
    override fun find(code: String): FallbackCapacityPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCapacityPreference) {
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

    override fun list(): MutableList<FallbackCapacityPreference> {
        val out = mutableListOf<FallbackCapacityPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCapacityPreferenceStore(): FallbackCapacityPreferenceStore {
    return MemoryFallbackCapacityPreferenceStore(
        mutableMapOf<String, FallbackCapacityPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCapacityPreferenceStore(
    var inner: FallbackCapacityPreferenceStore,
    var cache: TtlCache<FallbackCapacityPreference>,
    var hits: Int,
    var misses: Int,
) : FallbackCapacityPreferenceStore {
    override fun find(code: String): FallbackCapacityPreference? {
        val hit = (cache.get(code) as FallbackCapacityPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCapacityPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCapacityPreference> = inner.list()
}

fun newCachedFallbackCapacityPreferenceStore(inner: FallbackCapacityPreferenceStore, ttl: Int, clock: Clock): FallbackCapacityPreferenceStore {
    return CachedFallbackCapacityPreferenceStore(
        inner = inner,
        cache = newTtlCache<FallbackCapacityPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback capacity preferences. Empty fields match all. */
data class FallbackCapacityPreferenceQuery(
    var depot: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCapacityPreference): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<FallbackCapacityPreference>): MutableList<FallbackCapacityPreference> {
        val matched = filter(items) { candidate: FallbackCapacityPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCapacityPreferences(store: FallbackCapacityPreferenceStore, query: FallbackCapacityPreferenceQuery): MutableList<FallbackCapacityPreference> {
    return query.apply(store.list())
}
