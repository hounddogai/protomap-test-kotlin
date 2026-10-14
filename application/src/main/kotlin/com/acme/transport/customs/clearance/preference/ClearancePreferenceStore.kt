package com.acme.transport.customs.clearance.preference

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance preferences by code. */
interface ClearancePreferenceStore {
    fun find(code: String): ClearancePreference?
    fun save(item: ClearancePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearancePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearancePreferenceStore(
    var items: MutableMap<String, ClearancePreference>,
    var order: MutableList<String>,
) : ClearancePreferenceStore {
    override fun find(code: String): ClearancePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearancePreference) {
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

    override fun list(): MutableList<ClearancePreference> {
        val out = mutableListOf<ClearancePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearancePreferenceStore(): ClearancePreferenceStore {
    return MemoryClearancePreferenceStore(mutableMapOf<String, ClearancePreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearancePreferenceStore(
    var inner: ClearancePreferenceStore,
    var cache: TtlCache<ClearancePreference>,
    var hits: Int,
    var misses: Int,
) : ClearancePreferenceStore {
    override fun find(code: String): ClearancePreference? {
        val hit = (cache.get(code) as ClearancePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearancePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearancePreference> = inner.list()
}

fun newCachedClearancePreferenceStore(inner: ClearancePreferenceStore, ttl: Int, clock: Clock): ClearancePreferenceStore {
    return CachedClearancePreferenceStore(
        inner = inner,
        cache = newTtlCache<ClearancePreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance preferences. Empty fields match all. */
data class ClearancePreferenceQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearancePreference): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearancePreference>): MutableList<ClearancePreference> {
        val matched = filter(items) { candidate: ClearancePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearancePreferences(store: ClearancePreferenceStore, query: ClearancePreferenceQuery): MutableList<ClearancePreference> {
    return query.apply(store.list())
}
