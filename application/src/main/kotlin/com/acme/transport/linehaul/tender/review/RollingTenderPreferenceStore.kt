package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling tender preferences by code. */
interface RollingTenderPreferenceStore {
    fun find(code: String): RollingTenderPreference?
    fun save(item: RollingTenderPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTenderPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTenderPreferenceStore(
    var items: MutableMap<String, RollingTenderPreference>,
    var order: MutableList<String>,
) : RollingTenderPreferenceStore {
    override fun find(code: String): RollingTenderPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTenderPreference) {
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

    override fun list(): MutableList<RollingTenderPreference> {
        val out = mutableListOf<RollingTenderPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTenderPreferenceStore(): RollingTenderPreferenceStore {
    return MemoryRollingTenderPreferenceStore(
        mutableMapOf<String, RollingTenderPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTenderPreferenceStore(
    var inner: RollingTenderPreferenceStore,
    var cache: TtlCache<RollingTenderPreference>,
    var hits: Int,
    var misses: Int,
) : RollingTenderPreferenceStore {
    override fun find(code: String): RollingTenderPreference? {
        val hit = (cache.get(code) as RollingTenderPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTenderPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTenderPreference> = inner.list()
}

fun newCachedRollingTenderPreferenceStore(inner: RollingTenderPreferenceStore, ttl: Int, clock: Clock): RollingTenderPreferenceStore {
    return CachedRollingTenderPreferenceStore(
        inner = inner,
        cache = newTtlCache<RollingTenderPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tender preferences. Empty fields match all. */
data class RollingTenderPreferenceQuery(
    var region: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTenderPreference): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTenderPreference>): MutableList<RollingTenderPreference> {
        val matched = filter(items) { candidate: RollingTenderPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTenderPreferences(store: RollingTenderPreferenceStore, query: RollingTenderPreferenceQuery): MutableList<RollingTenderPreference> {
    return query.apply(store.list())
}
