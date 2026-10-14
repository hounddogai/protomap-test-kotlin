package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling cutoff preferences by code. */
interface RollingCutoffPreferenceStore {
    fun find(code: String): RollingCutoffPreference?
    fun save(item: RollingCutoffPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCutoffPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCutoffPreferenceStore(
    var items: MutableMap<String, RollingCutoffPreference>,
    var order: MutableList<String>,
) : RollingCutoffPreferenceStore {
    override fun find(code: String): RollingCutoffPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCutoffPreference) {
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

    override fun list(): MutableList<RollingCutoffPreference> {
        val out = mutableListOf<RollingCutoffPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCutoffPreferenceStore(): RollingCutoffPreferenceStore {
    return MemoryRollingCutoffPreferenceStore(
        mutableMapOf<String, RollingCutoffPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCutoffPreferenceStore(
    var inner: RollingCutoffPreferenceStore,
    var cache: TtlCache<RollingCutoffPreference>,
    var hits: Int,
    var misses: Int,
) : RollingCutoffPreferenceStore {
    override fun find(code: String): RollingCutoffPreference? {
        val hit = (cache.get(code) as RollingCutoffPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCutoffPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCutoffPreference> = inner.list()
}

fun newCachedRollingCutoffPreferenceStore(inner: RollingCutoffPreferenceStore, ttl: Int, clock: Clock): RollingCutoffPreferenceStore {
    return CachedRollingCutoffPreferenceStore(
        inner = inner,
        cache = newTtlCache<RollingCutoffPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling cutoff preferences. Empty fields match all. */
data class RollingCutoffPreferenceQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCutoffPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCutoffPreference>): MutableList<RollingCutoffPreference> {
        val matched = filter(items) { candidate: RollingCutoffPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCutoffPreferences(store: RollingCutoffPreferenceStore, query: RollingCutoffPreferenceQuery): MutableList<RollingCutoffPreference> {
    return query.apply(store.list())
}
