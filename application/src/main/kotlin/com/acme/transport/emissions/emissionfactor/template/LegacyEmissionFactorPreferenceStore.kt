package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy emission factor preferences by code. */
interface LegacyEmissionFactorPreferenceStore {
    fun find(code: String): LegacyEmissionFactorPreference?
    fun save(item: LegacyEmissionFactorPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyEmissionFactorPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyEmissionFactorPreferenceStore(
    var items: MutableMap<String, LegacyEmissionFactorPreference>,
    var order: MutableList<String>,
) : LegacyEmissionFactorPreferenceStore {
    override fun find(code: String): LegacyEmissionFactorPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyEmissionFactorPreference) {
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

    override fun list(): MutableList<LegacyEmissionFactorPreference> {
        val out = mutableListOf<LegacyEmissionFactorPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyEmissionFactorPreferenceStore(): LegacyEmissionFactorPreferenceStore {
    return MemoryLegacyEmissionFactorPreferenceStore(
        mutableMapOf<String, LegacyEmissionFactorPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyEmissionFactorPreferenceStore(
    var inner: LegacyEmissionFactorPreferenceStore,
    var cache: TtlCache<LegacyEmissionFactorPreference>,
    var hits: Int,
    var misses: Int,
) : LegacyEmissionFactorPreferenceStore {
    override fun find(code: String): LegacyEmissionFactorPreference? {
        val hit = (cache.get(code) as LegacyEmissionFactorPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyEmissionFactorPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyEmissionFactorPreference> = inner.list()
}

fun newCachedLegacyEmissionFactorPreferenceStore(inner: LegacyEmissionFactorPreferenceStore, ttl: Int, clock: Clock): LegacyEmissionFactorPreferenceStore {
    return CachedLegacyEmissionFactorPreferenceStore(
        inner = inner,
        cache = newTtlCache<LegacyEmissionFactorPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy emission factor preferences. Empty fields match all. */
data class LegacyEmissionFactorPreferenceQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyEmissionFactorPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyEmissionFactorPreference>): MutableList<LegacyEmissionFactorPreference> {
        val matched = filter(items) { candidate: LegacyEmissionFactorPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyEmissionFactorPreferences(store: LegacyEmissionFactorPreferenceStore, query: LegacyEmissionFactorPreferenceQuery): MutableList<LegacyEmissionFactorPreference> {
    return query.apply(store.list())
}
