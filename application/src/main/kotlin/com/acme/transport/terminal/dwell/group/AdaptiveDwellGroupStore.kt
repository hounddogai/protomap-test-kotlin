package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive dwell groups by code. */
interface AdaptiveDwellGroupStore {
    fun find(code: String): AdaptiveDwellGroup?
    fun save(item: AdaptiveDwellGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDwellGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDwellGroupStore(
    var items: MutableMap<String, AdaptiveDwellGroup>,
    var order: MutableList<String>,
) : AdaptiveDwellGroupStore {
    override fun find(code: String): AdaptiveDwellGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDwellGroup) {
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

    override fun list(): MutableList<AdaptiveDwellGroup> {
        val out = mutableListOf<AdaptiveDwellGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDwellGroupStore(): AdaptiveDwellGroupStore {
    return MemoryAdaptiveDwellGroupStore(mutableMapOf<String, AdaptiveDwellGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDwellGroupStore(
    var inner: AdaptiveDwellGroupStore,
    var cache: TtlCache<AdaptiveDwellGroup>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDwellGroupStore {
    override fun find(code: String): AdaptiveDwellGroup? {
        val hit = (cache.get(code) as AdaptiveDwellGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDwellGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDwellGroup> = inner.list()
}

fun newCachedAdaptiveDwellGroupStore(inner: AdaptiveDwellGroupStore, ttl: Int, clock: Clock): AdaptiveDwellGroupStore {
    return CachedAdaptiveDwellGroupStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDwellGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dwell groups. Empty fields match all. */
data class AdaptiveDwellGroupQuery(
    var category: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDwellGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDwellGroup>): MutableList<AdaptiveDwellGroup> {
        val matched = filter(items) { candidate: AdaptiveDwellGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDwellGroups(store: AdaptiveDwellGroupStore, query: AdaptiveDwellGroupQuery): MutableList<AdaptiveDwellGroup> {
    return query.apply(store.list())
}
