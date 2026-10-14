package com.acme.transport.safety.incident.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident preferences by code. */
interface IncidentPreferenceStore {
    fun find(code: String): IncidentPreference?
    fun save(item: IncidentPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentPreferenceStore(
    var items: MutableMap<String, IncidentPreference>,
    var order: MutableList<String>,
) : IncidentPreferenceStore {
    override fun find(code: String): IncidentPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentPreference) {
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

    override fun list(): MutableList<IncidentPreference> {
        val out = mutableListOf<IncidentPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentPreferenceStore(): IncidentPreferenceStore {
    return MemoryIncidentPreferenceStore(mutableMapOf<String, IncidentPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentPreferenceStore(
    var inner: IncidentPreferenceStore,
    var cache: TtlCache<IncidentPreference>,
    var hits: Int,
    var misses: Int,
) : IncidentPreferenceStore {
    override fun find(code: String): IncidentPreference? {
        val hit = (cache.get(code) as IncidentPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentPreference> = inner.list()
}

fun newCachedIncidentPreferenceStore(inner: IncidentPreferenceStore, ttl: Int, clock: Clock): IncidentPreferenceStore {
    return CachedIncidentPreferenceStore(
        inner = inner,
        cache = newTtlCache<IncidentPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident preferences. Empty fields match all. */
data class IncidentPreferenceQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentPreference>): MutableList<IncidentPreference> {
        val matched = filter(items) { candidate: IncidentPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentPreferences(store: IncidentPreferenceStore, query: IncidentPreferenceQuery): MutableList<IncidentPreference> {
    return query.apply(store.list())
}
