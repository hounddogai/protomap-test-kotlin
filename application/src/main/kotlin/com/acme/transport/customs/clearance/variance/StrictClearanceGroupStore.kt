package com.acme.transport.customs.clearance.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict clearance groups by code. */
interface StrictClearanceGroupStore {
    fun find(code: String): StrictClearanceGroup?
    fun save(item: StrictClearanceGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictClearanceGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictClearanceGroupStore(
    var items: MutableMap<String, StrictClearanceGroup>,
    var order: MutableList<String>,
) : StrictClearanceGroupStore {
    override fun find(code: String): StrictClearanceGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictClearanceGroup) {
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

    override fun list(): MutableList<StrictClearanceGroup> {
        val out = mutableListOf<StrictClearanceGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictClearanceGroupStore(): StrictClearanceGroupStore {
    return MemoryStrictClearanceGroupStore(
        mutableMapOf<String, StrictClearanceGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictClearanceGroupStore(
    var inner: StrictClearanceGroupStore,
    var cache: TtlCache<StrictClearanceGroup>,
    var hits: Int,
    var misses: Int,
) : StrictClearanceGroupStore {
    override fun find(code: String): StrictClearanceGroup? {
        val hit = (cache.get(code) as StrictClearanceGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictClearanceGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictClearanceGroup> = inner.list()
}

fun newCachedStrictClearanceGroupStore(inner: StrictClearanceGroupStore, ttl: Int, clock: Clock): StrictClearanceGroupStore {
    return CachedStrictClearanceGroupStore(
        inner = inner,
        cache = newTtlCache<StrictClearanceGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict clearance groups. Empty fields match all. */
data class StrictClearanceGroupQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictClearanceGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictClearanceGroup>): MutableList<StrictClearanceGroup> {
        val matched = filter(items) { candidate: StrictClearanceGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictClearanceGroups(store: StrictClearanceGroupStore, query: StrictClearanceGroupQuery): MutableList<StrictClearanceGroup> {
    return query.apply(store.list())
}
