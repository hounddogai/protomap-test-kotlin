package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default node allocations by code. */
interface DefaultNodeAllocationStore {
    fun find(code: String): DefaultNodeAllocation?
    fun save(item: DefaultNodeAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultNodeAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultNodeAllocationStore(
    var items: MutableMap<String, DefaultNodeAllocation>,
    var order: MutableList<String>,
) : DefaultNodeAllocationStore {
    override fun find(code: String): DefaultNodeAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultNodeAllocation) {
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

    override fun list(): MutableList<DefaultNodeAllocation> {
        val out = mutableListOf<DefaultNodeAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultNodeAllocationStore(): DefaultNodeAllocationStore {
    return MemoryDefaultNodeAllocationStore(
        mutableMapOf<String, DefaultNodeAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultNodeAllocationStore(
    var inner: DefaultNodeAllocationStore,
    var cache: TtlCache<DefaultNodeAllocation>,
    var hits: Int,
    var misses: Int,
) : DefaultNodeAllocationStore {
    override fun find(code: String): DefaultNodeAllocation? {
        val hit = (cache.get(code) as DefaultNodeAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultNodeAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultNodeAllocation> = inner.list()
}

fun newCachedDefaultNodeAllocationStore(inner: DefaultNodeAllocationStore, ttl: Int, clock: Clock): DefaultNodeAllocationStore {
    return CachedDefaultNodeAllocationStore(
        inner = inner,
        cache = newTtlCache<DefaultNodeAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default node allocations. Empty fields match all. */
data class DefaultNodeAllocationQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultNodeAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultNodeAllocation>): MutableList<DefaultNodeAllocation> {
        val matched = filter(items) { candidate: DefaultNodeAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultNodeAllocations(store: DefaultNodeAllocationStore, query: DefaultNodeAllocationQuery): MutableList<DefaultNodeAllocation> {
    return query.apply(store.list())
}
