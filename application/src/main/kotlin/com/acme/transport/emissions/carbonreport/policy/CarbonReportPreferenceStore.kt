package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report preferences by code. */
interface CarbonReportPreferenceStore {
    fun find(code: String): CarbonReportPreference?
    fun save(item: CarbonReportPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportPreferenceStore(
    var items: MutableMap<String, CarbonReportPreference>,
    var order: MutableList<String>,
) : CarbonReportPreferenceStore {
    override fun find(code: String): CarbonReportPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportPreference) {
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

    override fun list(): MutableList<CarbonReportPreference> {
        val out = mutableListOf<CarbonReportPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportPreferenceStore(): CarbonReportPreferenceStore {
    return MemoryCarbonReportPreferenceStore(
        mutableMapOf<String, CarbonReportPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportPreferenceStore(
    var inner: CarbonReportPreferenceStore,
    var cache: TtlCache<CarbonReportPreference>,
    var hits: Int,
    var misses: Int,
) : CarbonReportPreferenceStore {
    override fun find(code: String): CarbonReportPreference? {
        val hit = (cache.get(code) as CarbonReportPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportPreference> = inner.list()
}

fun newCachedCarbonReportPreferenceStore(inner: CarbonReportPreferenceStore, ttl: Int, clock: Clock): CarbonReportPreferenceStore {
    return CachedCarbonReportPreferenceStore(
        inner = inner,
        cache = newTtlCache<CarbonReportPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report preferences. Empty fields match all. */
data class CarbonReportPreferenceQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportPreference>): MutableList<CarbonReportPreference> {
        val matched = filter(items) { candidate: CarbonReportPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportPreferences(store: CarbonReportPreferenceStore, query: CarbonReportPreferenceQuery): MutableList<CarbonReportPreference> {
    return query.apply(store.list())
}
