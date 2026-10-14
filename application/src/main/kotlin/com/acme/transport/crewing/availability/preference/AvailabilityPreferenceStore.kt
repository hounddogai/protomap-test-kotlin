package com.acme.transport.crewing.availability.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability preferences by code. */
interface AvailabilityPreferenceStore {
    fun find(code: String): AvailabilityPreference?
    fun save(item: AvailabilityPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityPreferenceStore(
    var items: MutableMap<String, AvailabilityPreference>,
    var order: MutableList<String>,
) : AvailabilityPreferenceStore {
    override fun find(code: String): AvailabilityPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityPreference) {
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

    override fun list(): MutableList<AvailabilityPreference> {
        val out = mutableListOf<AvailabilityPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityPreferenceStore(): AvailabilityPreferenceStore {
    return MemoryAvailabilityPreferenceStore(
        mutableMapOf<String, AvailabilityPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityPreferenceStore(
    var inner: AvailabilityPreferenceStore,
    var cache: TtlCache<AvailabilityPreference>,
    var hits: Int,
    var misses: Int,
) : AvailabilityPreferenceStore {
    override fun find(code: String): AvailabilityPreference? {
        val hit = (cache.get(code) as AvailabilityPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityPreference> = inner.list()
}

fun newCachedAvailabilityPreferenceStore(inner: AvailabilityPreferenceStore, ttl: Int, clock: Clock): AvailabilityPreferenceStore {
    return CachedAvailabilityPreferenceStore(
        inner = inner,
        cache = newTtlCache<AvailabilityPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability preferences. Empty fields match all. */
data class AvailabilityPreferenceQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityPreference>): MutableList<AvailabilityPreference> {
        val matched = filter(items) { candidate: AvailabilityPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityPreferences(store: AvailabilityPreferenceStore, query: AvailabilityPreferenceQuery): MutableList<AvailabilityPreference> {
    return query.apply(store.list())
}
