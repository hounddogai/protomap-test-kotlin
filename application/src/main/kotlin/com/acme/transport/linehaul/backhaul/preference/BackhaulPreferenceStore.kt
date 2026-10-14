package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul preferences by code. */
interface BackhaulPreferenceStore {
    fun find(code: String): BackhaulPreference?
    fun save(item: BackhaulPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulPreferenceStore(
    var items: MutableMap<String, BackhaulPreference>,
    var order: MutableList<String>,
) : BackhaulPreferenceStore {
    override fun find(code: String): BackhaulPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulPreference) {
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

    override fun list(): MutableList<BackhaulPreference> {
        val out = mutableListOf<BackhaulPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulPreferenceStore(): BackhaulPreferenceStore {
    return MemoryBackhaulPreferenceStore(mutableMapOf<String, BackhaulPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulPreferenceStore(
    var inner: BackhaulPreferenceStore,
    var cache: TtlCache<BackhaulPreference>,
    var hits: Int,
    var misses: Int,
) : BackhaulPreferenceStore {
    override fun find(code: String): BackhaulPreference? {
        val hit = (cache.get(code) as BackhaulPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulPreference> = inner.list()
}

fun newCachedBackhaulPreferenceStore(inner: BackhaulPreferenceStore, ttl: Int, clock: Clock): BackhaulPreferenceStore {
    return CachedBackhaulPreferenceStore(
        inner = inner,
        cache = newTtlCache<BackhaulPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul preferences. Empty fields match all. */
data class BackhaulPreferenceQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulPreference): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulPreference>): MutableList<BackhaulPreference> {
        val matched = filter(items) { candidate: BackhaulPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulPreferences(store: BackhaulPreferenceStore, query: BackhaulPreferenceQuery): MutableList<BackhaulPreference> {
    return query.apply(store.list())
}
