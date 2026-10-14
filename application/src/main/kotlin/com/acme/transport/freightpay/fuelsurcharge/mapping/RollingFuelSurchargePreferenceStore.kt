package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel surcharge preferences by code. */
interface RollingFuelSurchargePreferenceStore {
    fun find(code: String): RollingFuelSurchargePreference?
    fun save(item: RollingFuelSurchargePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelSurchargePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelSurchargePreferenceStore(
    var items: MutableMap<String, RollingFuelSurchargePreference>,
    var order: MutableList<String>,
) : RollingFuelSurchargePreferenceStore {
    override fun find(code: String): RollingFuelSurchargePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelSurchargePreference) {
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

    override fun list(): MutableList<RollingFuelSurchargePreference> {
        val out = mutableListOf<RollingFuelSurchargePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelSurchargePreferenceStore(): RollingFuelSurchargePreferenceStore {
    return MemoryRollingFuelSurchargePreferenceStore(
        mutableMapOf<String, RollingFuelSurchargePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelSurchargePreferenceStore(
    var inner: RollingFuelSurchargePreferenceStore,
    var cache: TtlCache<RollingFuelSurchargePreference>,
    var hits: Int,
    var misses: Int,
) : RollingFuelSurchargePreferenceStore {
    override fun find(code: String): RollingFuelSurchargePreference? {
        val hit = (cache.get(code) as RollingFuelSurchargePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelSurchargePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelSurchargePreference> = inner.list()
}

fun newCachedRollingFuelSurchargePreferenceStore(inner: RollingFuelSurchargePreferenceStore, ttl: Int, clock: Clock): RollingFuelSurchargePreferenceStore {
    return CachedRollingFuelSurchargePreferenceStore(
        inner = inner,
        cache = newTtlCache<RollingFuelSurchargePreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel surcharge preferences. Empty fields match all. */
data class RollingFuelSurchargePreferenceQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelSurchargePreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingFuelSurchargePreference>): MutableList<RollingFuelSurchargePreference> {
        val matched = filter(items) { candidate: RollingFuelSurchargePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelSurchargePreferences(store: RollingFuelSurchargePreferenceStore, query: RollingFuelSurchargePreferenceQuery): MutableList<RollingFuelSurchargePreference> {
    return query.apply(store.list())
}
