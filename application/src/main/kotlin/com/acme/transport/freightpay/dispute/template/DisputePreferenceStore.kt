package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute preferences by code. */
interface DisputePreferenceStore {
    fun find(code: String): DisputePreference?
    fun save(item: DisputePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputePreferenceStore(
    var items: MutableMap<String, DisputePreference>,
    var order: MutableList<String>,
) : DisputePreferenceStore {
    override fun find(code: String): DisputePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputePreference) {
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

    override fun list(): MutableList<DisputePreference> {
        val out = mutableListOf<DisputePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputePreferenceStore(): DisputePreferenceStore {
    return MemoryDisputePreferenceStore(mutableMapOf<String, DisputePreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputePreferenceStore(
    var inner: DisputePreferenceStore,
    var cache: TtlCache<DisputePreference>,
    var hits: Int,
    var misses: Int,
) : DisputePreferenceStore {
    override fun find(code: String): DisputePreference? {
        val hit = (cache.get(code) as DisputePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputePreference> = inner.list()
}

fun newCachedDisputePreferenceStore(inner: DisputePreferenceStore, ttl: Int, clock: Clock): DisputePreferenceStore {
    return CachedDisputePreferenceStore(
        inner = inner,
        cache = newTtlCache<DisputePreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute preferences. Empty fields match all. */
data class DisputePreferenceQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputePreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DisputePreference>): MutableList<DisputePreference> {
        val matched = filter(items) { candidate: DisputePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputePreferences(store: DisputePreferenceStore, query: DisputePreferenceQuery): MutableList<DisputePreference> {
    return query.apply(store.list())
}
