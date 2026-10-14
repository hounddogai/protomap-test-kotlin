package com.acme.transport.customs.clearance.target

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary clearance groups by code. */
interface PrimaryClearanceGroupStore {
    fun find(code: String): PrimaryClearanceGroup?
    fun save(item: PrimaryClearanceGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryClearanceGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryClearanceGroupStore(
    var items: MutableMap<String, PrimaryClearanceGroup>,
    var order: MutableList<String>,
) : PrimaryClearanceGroupStore {
    override fun find(code: String): PrimaryClearanceGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryClearanceGroup) {
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

    override fun list(): MutableList<PrimaryClearanceGroup> {
        val out = mutableListOf<PrimaryClearanceGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryClearanceGroupStore(): PrimaryClearanceGroupStore {
    return MemoryPrimaryClearanceGroupStore(
        mutableMapOf<String, PrimaryClearanceGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryClearanceGroupStore(
    var inner: PrimaryClearanceGroupStore,
    var cache: TtlCache<PrimaryClearanceGroup>,
    var hits: Int,
    var misses: Int,
) : PrimaryClearanceGroupStore {
    override fun find(code: String): PrimaryClearanceGroup? {
        val hit = (cache.get(code) as PrimaryClearanceGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryClearanceGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryClearanceGroup> = inner.list()
}

fun newCachedPrimaryClearanceGroupStore(inner: PrimaryClearanceGroupStore, ttl: Int, clock: Clock): PrimaryClearanceGroupStore {
    return CachedPrimaryClearanceGroupStore(
        inner = inner,
        cache = newTtlCache<PrimaryClearanceGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary clearance groups. Empty fields match all. */
data class PrimaryClearanceGroupQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryClearanceGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryClearanceGroup>): MutableList<PrimaryClearanceGroup> {
        val matched = filter(items) { candidate: PrimaryClearanceGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryClearanceGroups(store: PrimaryClearanceGroupStore, query: PrimaryClearanceGroupQuery): MutableList<PrimaryClearanceGroup> {
    return query.apply(store.list())
}
