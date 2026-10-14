package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default accessorial allocations by code. */
interface DefaultAccessorialAllocationStore {
    fun find(code: String): DefaultAccessorialAllocation?
    fun save(item: DefaultAccessorialAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAccessorialAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAccessorialAllocationStore(
    var items: MutableMap<String, DefaultAccessorialAllocation>,
    var order: MutableList<String>,
) : DefaultAccessorialAllocationStore {
    override fun find(code: String): DefaultAccessorialAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAccessorialAllocation) {
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

    override fun list(): MutableList<DefaultAccessorialAllocation> {
        val out = mutableListOf<DefaultAccessorialAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAccessorialAllocationStore(): DefaultAccessorialAllocationStore {
    return MemoryDefaultAccessorialAllocationStore(
        mutableMapOf<String, DefaultAccessorialAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAccessorialAllocationStore(
    var inner: DefaultAccessorialAllocationStore,
    var cache: TtlCache<DefaultAccessorialAllocation>,
    var hits: Int,
    var misses: Int,
) : DefaultAccessorialAllocationStore {
    override fun find(code: String): DefaultAccessorialAllocation? {
        val hit = (cache.get(code) as DefaultAccessorialAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAccessorialAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAccessorialAllocation> = inner.list()
}

fun newCachedDefaultAccessorialAllocationStore(inner: DefaultAccessorialAllocationStore, ttl: Int, clock: Clock): DefaultAccessorialAllocationStore {
    return CachedDefaultAccessorialAllocationStore(
        inner = inner,
        cache = newTtlCache<DefaultAccessorialAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default accessorial allocations. Empty fields match all. */
data class DefaultAccessorialAllocationQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAccessorialAllocation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<DefaultAccessorialAllocation>): MutableList<DefaultAccessorialAllocation> {
        val matched = filter(items) { candidate: DefaultAccessorialAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAccessorialAllocations(store: DefaultAccessorialAllocationStore, query: DefaultAccessorialAllocationQuery): MutableList<DefaultAccessorialAllocation> {
    return query.apply(store.list())
}
