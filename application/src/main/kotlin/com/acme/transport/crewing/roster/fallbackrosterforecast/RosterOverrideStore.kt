package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster overrides by code. */
interface RosterOverrideStore {
    fun find(code: String): RosterOverride?
    fun save(item: RosterOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterOverrideStore(
    var items: MutableMap<String, RosterOverride>,
    var order: MutableList<String>,
) : RosterOverrideStore {
    override fun find(code: String): RosterOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterOverride) {
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

    override fun list(): MutableList<RosterOverride> {
        val out = mutableListOf<RosterOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterOverrideStore(): RosterOverrideStore {
    return MemoryRosterOverrideStore(mutableMapOf<String, RosterOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterOverrideStore(
    var inner: RosterOverrideStore,
    var cache: TtlCache<RosterOverride>,
    var hits: Int,
    var misses: Int,
) : RosterOverrideStore {
    override fun find(code: String): RosterOverride? {
        val hit = (cache.get(code) as RosterOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterOverride> = inner.list()
}

fun newCachedRosterOverrideStore(inner: RosterOverrideStore, ttl: Int, clock: Clock): RosterOverrideStore {
    return CachedRosterOverrideStore(
        inner = inner,
        cache = newTtlCache<RosterOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster overrides. Empty fields match all. */
data class RosterOverrideQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterOverride): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterOverride>): MutableList<RosterOverride> {
        val matched = filter(items) { candidate: RosterOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterOverrides(store: RosterOverrideStore, query: RosterOverrideQuery): MutableList<RosterOverride> {
    return query.apply(store.list())
}
