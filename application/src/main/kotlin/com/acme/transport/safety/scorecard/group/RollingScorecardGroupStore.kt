package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling scorecard groups by code. */
interface RollingScorecardGroupStore {
    fun find(code: String): RollingScorecardGroup?
    fun save(item: RollingScorecardGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingScorecardGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingScorecardGroupStore(
    var items: MutableMap<String, RollingScorecardGroup>,
    var order: MutableList<String>,
) : RollingScorecardGroupStore {
    override fun find(code: String): RollingScorecardGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingScorecardGroup) {
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

    override fun list(): MutableList<RollingScorecardGroup> {
        val out = mutableListOf<RollingScorecardGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingScorecardGroupStore(): RollingScorecardGroupStore {
    return MemoryRollingScorecardGroupStore(
        mutableMapOf<String, RollingScorecardGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingScorecardGroupStore(
    var inner: RollingScorecardGroupStore,
    var cache: TtlCache<RollingScorecardGroup>,
    var hits: Int,
    var misses: Int,
) : RollingScorecardGroupStore {
    override fun find(code: String): RollingScorecardGroup? {
        val hit = (cache.get(code) as RollingScorecardGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingScorecardGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingScorecardGroup> = inner.list()
}

fun newCachedRollingScorecardGroupStore(inner: RollingScorecardGroupStore, ttl: Int, clock: Clock): RollingScorecardGroupStore {
    return CachedRollingScorecardGroupStore(
        inner = inner,
        cache = newTtlCache<RollingScorecardGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling scorecard groups. Empty fields match all. */
data class RollingScorecardGroupQuery(
    var locale: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingScorecardGroup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingScorecardGroup>): MutableList<RollingScorecardGroup> {
        val matched = filter(items) { candidate: RollingScorecardGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingScorecardGroups(store: RollingScorecardGroupStore, query: RollingScorecardGroupQuery): MutableList<RollingScorecardGroup> {
    return query.apply(store.list())
}
