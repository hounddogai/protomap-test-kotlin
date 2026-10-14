package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break groups by code. */
interface RestBreakGroupStore {
    fun find(code: String): RestBreakGroup?
    fun save(item: RestBreakGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakGroupStore(
    var items: MutableMap<String, RestBreakGroup>,
    var order: MutableList<String>,
) : RestBreakGroupStore {
    override fun find(code: String): RestBreakGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakGroup) {
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

    override fun list(): MutableList<RestBreakGroup> {
        val out = mutableListOf<RestBreakGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakGroupStore(): RestBreakGroupStore {
    return MemoryRestBreakGroupStore(mutableMapOf<String, RestBreakGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakGroupStore(
    var inner: RestBreakGroupStore,
    var cache: TtlCache<RestBreakGroup>,
    var hits: Int,
    var misses: Int,
) : RestBreakGroupStore {
    override fun find(code: String): RestBreakGroup? {
        val hit = (cache.get(code) as RestBreakGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakGroup> = inner.list()
}

fun newCachedRestBreakGroupStore(inner: RestBreakGroupStore, ttl: Int, clock: Clock): RestBreakGroupStore {
    return CachedRestBreakGroupStore(
        inner = inner,
        cache = newTtlCache<RestBreakGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break groups. Empty fields match all. */
data class RestBreakGroupQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RestBreakGroup>): MutableList<RestBreakGroup> {
        val matched = filter(items) { candidate: RestBreakGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakGroups(store: RestBreakGroupStore, query: RestBreakGroupQuery): MutableList<RestBreakGroup> {
    return query.apply(store.list())
}
