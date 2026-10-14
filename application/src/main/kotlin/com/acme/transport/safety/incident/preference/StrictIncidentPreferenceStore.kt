package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict incident preferences by code. */
interface StrictIncidentPreferenceStore {
    fun find(code: String): StrictIncidentPreference?
    fun save(item: StrictIncidentPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIncidentPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIncidentPreferenceStore(
    var items: MutableMap<String, StrictIncidentPreference>,
    var order: MutableList<String>,
) : StrictIncidentPreferenceStore {
    override fun find(code: String): StrictIncidentPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIncidentPreference) {
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

    override fun list(): MutableList<StrictIncidentPreference> {
        val out = mutableListOf<StrictIncidentPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIncidentPreferenceStore(): StrictIncidentPreferenceStore {
    return MemoryStrictIncidentPreferenceStore(
        mutableMapOf<String, StrictIncidentPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIncidentPreferenceStore(
    var inner: StrictIncidentPreferenceStore,
    var cache: TtlCache<StrictIncidentPreference>,
    var hits: Int,
    var misses: Int,
) : StrictIncidentPreferenceStore {
    override fun find(code: String): StrictIncidentPreference? {
        val hit = (cache.get(code) as StrictIncidentPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIncidentPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIncidentPreference> = inner.list()
}

fun newCachedStrictIncidentPreferenceStore(inner: StrictIncidentPreferenceStore, ttl: Int, clock: Clock): StrictIncidentPreferenceStore {
    return CachedStrictIncidentPreferenceStore(
        inner = inner,
        cache = newTtlCache<StrictIncidentPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict incident preferences. Empty fields match all. */
data class StrictIncidentPreferenceQuery(
    var category: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIncidentPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictIncidentPreference>): MutableList<StrictIncidentPreference> {
        val matched = filter(items) { candidate: StrictIncidentPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIncidentPreferences(store: StrictIncidentPreferenceStore, query: StrictIncidentPreferenceQuery): MutableList<StrictIncidentPreference> {
    return query.apply(store.list())
}
