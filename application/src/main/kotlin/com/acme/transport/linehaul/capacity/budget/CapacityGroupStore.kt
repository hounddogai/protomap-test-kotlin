package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity groups by code. */
interface CapacityGroupStore {
    fun find(code: String): CapacityGroup?
    fun save(item: CapacityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityGroupStore(
    var items: MutableMap<String, CapacityGroup>,
    var order: MutableList<String>,
) : CapacityGroupStore {
    override fun find(code: String): CapacityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityGroup) {
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

    override fun list(): MutableList<CapacityGroup> {
        val out = mutableListOf<CapacityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityGroupStore(): CapacityGroupStore {
    return MemoryCapacityGroupStore(mutableMapOf<String, CapacityGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityGroupStore(
    var inner: CapacityGroupStore,
    var cache: TtlCache<CapacityGroup>,
    var hits: Int,
    var misses: Int,
) : CapacityGroupStore {
    override fun find(code: String): CapacityGroup? {
        val hit = (cache.get(code) as CapacityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityGroup> = inner.list()
}

fun newCachedCapacityGroupStore(inner: CapacityGroupStore, ttl: Int, clock: Clock): CapacityGroupStore {
    return CachedCapacityGroupStore(
        inner = inner,
        cache = newTtlCache<CapacityGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity groups. Empty fields match all. */
data class CapacityGroupQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityGroup): Boolean {
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

    fun apply(items: MutableList<CapacityGroup>): MutableList<CapacityGroup> {
        val matched = filter(items) { candidate: CapacityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityGroups(store: CapacityGroupStore, query: CapacityGroupQuery): MutableList<CapacityGroup> {
    return query.apply(store.list())
}
