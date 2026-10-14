package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return preferences by code. */
interface LeaseReturnPreferenceStore {
    fun find(code: String): LeaseReturnPreference?
    fun save(item: LeaseReturnPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnPreferenceStore(
    var items: MutableMap<String, LeaseReturnPreference>,
    var order: MutableList<String>,
) : LeaseReturnPreferenceStore {
    override fun find(code: String): LeaseReturnPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnPreference) {
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

    override fun list(): MutableList<LeaseReturnPreference> {
        val out = mutableListOf<LeaseReturnPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnPreferenceStore(): LeaseReturnPreferenceStore {
    return MemoryLeaseReturnPreferenceStore(
        mutableMapOf<String, LeaseReturnPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnPreferenceStore(
    var inner: LeaseReturnPreferenceStore,
    var cache: TtlCache<LeaseReturnPreference>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnPreferenceStore {
    override fun find(code: String): LeaseReturnPreference? {
        val hit = (cache.get(code) as LeaseReturnPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnPreference> = inner.list()
}

fun newCachedLeaseReturnPreferenceStore(inner: LeaseReturnPreferenceStore, ttl: Int, clock: Clock): LeaseReturnPreferenceStore {
    return CachedLeaseReturnPreferenceStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return preferences. Empty fields match all. */
data class LeaseReturnPreferenceQuery(
    var unit: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnPreference): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnPreference>): MutableList<LeaseReturnPreference> {
        val matched = filter(items) { candidate: LeaseReturnPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnPreferences(store: LeaseReturnPreferenceStore, query: LeaseReturnPreferenceQuery): MutableList<LeaseReturnPreference> {
    return query.apply(store.list())
}
