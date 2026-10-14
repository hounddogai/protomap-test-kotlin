package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance groups by code. */
interface ClearanceGroupStore {
    fun find(code: String): ClearanceGroup?
    fun save(item: ClearanceGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceGroupStore(
    var items: MutableMap<String, ClearanceGroup>,
    var order: MutableList<String>,
) : ClearanceGroupStore {
    override fun find(code: String): ClearanceGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceGroup) {
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

    override fun list(): MutableList<ClearanceGroup> {
        val out = mutableListOf<ClearanceGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceGroupStore(): ClearanceGroupStore {
    return MemoryClearanceGroupStore(mutableMapOf<String, ClearanceGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceGroupStore(
    var inner: ClearanceGroupStore,
    var cache: TtlCache<ClearanceGroup>,
    var hits: Int,
    var misses: Int,
) : ClearanceGroupStore {
    override fun find(code: String): ClearanceGroup? {
        val hit = (cache.get(code) as ClearanceGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceGroup> = inner.list()
}

fun newCachedClearanceGroupStore(inner: ClearanceGroupStore, ttl: Int, clock: Clock): ClearanceGroupStore {
    return CachedClearanceGroupStore(
        inner = inner,
        cache = newTtlCache<ClearanceGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance groups. Empty fields match all. */
data class ClearanceGroupQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceGroup>): MutableList<ClearanceGroup> {
        val matched = filter(items) { candidate: ClearanceGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceGroups(store: ClearanceGroupStore, query: ClearanceGroupQuery): MutableList<ClearanceGroup> {
    return query.apply(store.list())
}
