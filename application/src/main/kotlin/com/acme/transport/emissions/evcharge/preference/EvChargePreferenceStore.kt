package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge preferences by code. */
interface EvChargePreferenceStore {
    fun find(code: String): EvChargePreference?
    fun save(item: EvChargePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargePreferenceStore(
    var items: MutableMap<String, EvChargePreference>,
    var order: MutableList<String>,
) : EvChargePreferenceStore {
    override fun find(code: String): EvChargePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargePreference) {
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

    override fun list(): MutableList<EvChargePreference> {
        val out = mutableListOf<EvChargePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargePreferenceStore(): EvChargePreferenceStore {
    return MemoryEvChargePreferenceStore(mutableMapOf<String, EvChargePreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargePreferenceStore(
    var inner: EvChargePreferenceStore,
    var cache: TtlCache<EvChargePreference>,
    var hits: Int,
    var misses: Int,
) : EvChargePreferenceStore {
    override fun find(code: String): EvChargePreference? {
        val hit = (cache.get(code) as EvChargePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargePreference> = inner.list()
}

fun newCachedEvChargePreferenceStore(inner: EvChargePreferenceStore, ttl: Int, clock: Clock): EvChargePreferenceStore {
    return CachedEvChargePreferenceStore(
        inner = inner,
        cache = newTtlCache<EvChargePreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge preferences. Empty fields match all. */
data class EvChargePreferenceQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargePreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargePreference>): MutableList<EvChargePreference> {
        val matched = filter(items) { candidate: EvChargePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargePreferences(store: EvChargePreferenceStore, query: EvChargePreferenceQuery): MutableList<EvChargePreference> {
    return query.apply(store.list())
}
