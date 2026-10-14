package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy backhaul preferences by code. */
interface LegacyBackhaulPreferenceStore {
    fun find(code: String): LegacyBackhaulPreference?
    fun save(item: LegacyBackhaulPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBackhaulPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBackhaulPreferenceStore(
    var items: MutableMap<String, LegacyBackhaulPreference>,
    var order: MutableList<String>,
) : LegacyBackhaulPreferenceStore {
    override fun find(code: String): LegacyBackhaulPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBackhaulPreference) {
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

    override fun list(): MutableList<LegacyBackhaulPreference> {
        val out = mutableListOf<LegacyBackhaulPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBackhaulPreferenceStore(): LegacyBackhaulPreferenceStore {
    return MemoryLegacyBackhaulPreferenceStore(
        mutableMapOf<String, LegacyBackhaulPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBackhaulPreferenceStore(
    var inner: LegacyBackhaulPreferenceStore,
    var cache: TtlCache<LegacyBackhaulPreference>,
    var hits: Int,
    var misses: Int,
) : LegacyBackhaulPreferenceStore {
    override fun find(code: String): LegacyBackhaulPreference? {
        val hit = (cache.get(code) as LegacyBackhaulPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBackhaulPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBackhaulPreference> = inner.list()
}

fun newCachedLegacyBackhaulPreferenceStore(inner: LegacyBackhaulPreferenceStore, ttl: Int, clock: Clock): LegacyBackhaulPreferenceStore {
    return CachedLegacyBackhaulPreferenceStore(
        inner = inner,
        cache = newTtlCache<LegacyBackhaulPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy backhaul preferences. Empty fields match all. */
data class LegacyBackhaulPreferenceQuery(
    var segment: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBackhaulPreference): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBackhaulPreference>): MutableList<LegacyBackhaulPreference> {
        val matched = filter(items) { candidate: LegacyBackhaulPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBackhaulPreferences(store: LegacyBackhaulPreferenceStore, query: LegacyBackhaulPreferenceQuery): MutableList<LegacyBackhaulPreference> {
    return query.apply(store.list())
}
