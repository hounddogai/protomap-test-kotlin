package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy incident preferences by code. */
interface LegacyIncidentPreferenceStore {
    fun find(code: String): LegacyIncidentPreference?
    fun save(item: LegacyIncidentPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyIncidentPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyIncidentPreferenceStore(
    var items: MutableMap<String, LegacyIncidentPreference>,
    var order: MutableList<String>,
) : LegacyIncidentPreferenceStore {
    override fun find(code: String): LegacyIncidentPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyIncidentPreference) {
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

    override fun list(): MutableList<LegacyIncidentPreference> {
        val out = mutableListOf<LegacyIncidentPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyIncidentPreferenceStore(): LegacyIncidentPreferenceStore {
    return MemoryLegacyIncidentPreferenceStore(
        mutableMapOf<String, LegacyIncidentPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyIncidentPreferenceStore(
    var inner: LegacyIncidentPreferenceStore,
    var cache: TtlCache<LegacyIncidentPreference>,
    var hits: Int,
    var misses: Int,
) : LegacyIncidentPreferenceStore {
    override fun find(code: String): LegacyIncidentPreference? {
        val hit = (cache.get(code) as LegacyIncidentPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyIncidentPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyIncidentPreference> = inner.list()
}

fun newCachedLegacyIncidentPreferenceStore(inner: LegacyIncidentPreferenceStore, ttl: Int, clock: Clock): LegacyIncidentPreferenceStore {
    return CachedLegacyIncidentPreferenceStore(
        inner = inner,
        cache = newTtlCache<LegacyIncidentPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy incident preferences. Empty fields match all. */
data class LegacyIncidentPreferenceQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyIncidentPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyIncidentPreference>): MutableList<LegacyIncidentPreference> {
        val matched = filter(items) { candidate: LegacyIncidentPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyIncidentPreferences(store: LegacyIncidentPreferenceStore, query: LegacyIncidentPreferenceQuery): MutableList<LegacyIncidentPreference> {
    return query.apply(store.list())
}
