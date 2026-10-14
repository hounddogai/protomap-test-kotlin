package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending availability preferences by code. */
interface PendingAvailabilityPreferenceStore {
    fun find(code: String): PendingAvailabilityPreference?
    fun save(item: PendingAvailabilityPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAvailabilityPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAvailabilityPreferenceStore(
    var items: MutableMap<String, PendingAvailabilityPreference>,
    var order: MutableList<String>,
) : PendingAvailabilityPreferenceStore {
    override fun find(code: String): PendingAvailabilityPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAvailabilityPreference) {
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

    override fun list(): MutableList<PendingAvailabilityPreference> {
        val out = mutableListOf<PendingAvailabilityPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAvailabilityPreferenceStore(): PendingAvailabilityPreferenceStore {
    return MemoryPendingAvailabilityPreferenceStore(
        mutableMapOf<String, PendingAvailabilityPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAvailabilityPreferenceStore(
    var inner: PendingAvailabilityPreferenceStore,
    var cache: TtlCache<PendingAvailabilityPreference>,
    var hits: Int,
    var misses: Int,
) : PendingAvailabilityPreferenceStore {
    override fun find(code: String): PendingAvailabilityPreference? {
        val hit = (cache.get(code) as PendingAvailabilityPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAvailabilityPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAvailabilityPreference> = inner.list()
}

fun newCachedPendingAvailabilityPreferenceStore(inner: PendingAvailabilityPreferenceStore, ttl: Int, clock: Clock): PendingAvailabilityPreferenceStore {
    return CachedPendingAvailabilityPreferenceStore(
        inner = inner,
        cache = newTtlCache<PendingAvailabilityPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending availability preferences. Empty fields match all. */
data class PendingAvailabilityPreferenceQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAvailabilityPreference): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAvailabilityPreference>): MutableList<PendingAvailabilityPreference> {
        val matched = filter(items) { candidate: PendingAvailabilityPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAvailabilityPreferences(store: PendingAvailabilityPreferenceStore, query: PendingAvailabilityPreferenceQuery): MutableList<PendingAvailabilityPreference> {
    return query.apply(store.list())
}
