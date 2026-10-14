package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold preferences by code. */
interface HoldPreferenceStore {
    fun find(code: String): HoldPreference?
    fun save(item: HoldPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldPreferenceStore(
    var items: MutableMap<String, HoldPreference>,
    var order: MutableList<String>,
) : HoldPreferenceStore {
    override fun find(code: String): HoldPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldPreference) {
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

    override fun list(): MutableList<HoldPreference> {
        val out = mutableListOf<HoldPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldPreferenceStore(): HoldPreferenceStore {
    return MemoryHoldPreferenceStore(mutableMapOf<String, HoldPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldPreferenceStore(
    var inner: HoldPreferenceStore,
    var cache: TtlCache<HoldPreference>,
    var hits: Int,
    var misses: Int,
) : HoldPreferenceStore {
    override fun find(code: String): HoldPreference? {
        val hit = (cache.get(code) as HoldPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldPreference> = inner.list()
}

fun newCachedHoldPreferenceStore(inner: HoldPreferenceStore, ttl: Int, clock: Clock): HoldPreferenceStore {
    return CachedHoldPreferenceStore(
        inner = inner,
        cache = newTtlCache<HoldPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold preferences. Empty fields match all. */
data class HoldPreferenceQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldPreference): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<HoldPreference>): MutableList<HoldPreference> {
        val matched = filter(items) { candidate: HoldPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldPreferences(store: HoldPreferenceStore, query: HoldPreferenceQuery): MutableList<HoldPreference> {
    return query.apply(store.list())
}
