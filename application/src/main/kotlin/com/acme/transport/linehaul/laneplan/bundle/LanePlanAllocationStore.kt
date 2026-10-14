package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan allocations by code. */
interface LanePlanAllocationStore {
    fun find(code: String): LanePlanAllocation?
    fun save(item: LanePlanAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanAllocationStore(
    var items: MutableMap<String, LanePlanAllocation>,
    var order: MutableList<String>,
) : LanePlanAllocationStore {
    override fun find(code: String): LanePlanAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanAllocation) {
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

    override fun list(): MutableList<LanePlanAllocation> {
        val out = mutableListOf<LanePlanAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanAllocationStore(): LanePlanAllocationStore {
    return MemoryLanePlanAllocationStore(mutableMapOf<String, LanePlanAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanAllocationStore(
    var inner: LanePlanAllocationStore,
    var cache: TtlCache<LanePlanAllocation>,
    var hits: Int,
    var misses: Int,
) : LanePlanAllocationStore {
    override fun find(code: String): LanePlanAllocation? {
        val hit = (cache.get(code) as LanePlanAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanAllocation> = inner.list()
}

fun newCachedLanePlanAllocationStore(inner: LanePlanAllocationStore, ttl: Int, clock: Clock): LanePlanAllocationStore {
    return CachedLanePlanAllocationStore(
        inner = inner,
        cache = newTtlCache<LanePlanAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan allocations. Empty fields match all. */
data class LanePlanAllocationQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanAllocation>): MutableList<LanePlanAllocation> {
        val matched = filter(items) { candidate: LanePlanAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanAllocations(store: LanePlanAllocationStore, query: LanePlanAllocationQuery): MutableList<LanePlanAllocation> {
    return query.apply(store.list())
}
