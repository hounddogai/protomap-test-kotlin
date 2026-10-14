package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute rollups by code. */
interface DisputeRollupStore {
    fun find(code: String): DisputeRollup?
    fun save(item: DisputeRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeRollupStore(
    var items: MutableMap<String, DisputeRollup>,
    var order: MutableList<String>,
) : DisputeRollupStore {
    override fun find(code: String): DisputeRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeRollup) {
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

    override fun list(): MutableList<DisputeRollup> {
        val out = mutableListOf<DisputeRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeRollupStore(): DisputeRollupStore {
    return MemoryDisputeRollupStore(mutableMapOf<String, DisputeRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeRollupStore(
    var inner: DisputeRollupStore,
    var cache: TtlCache<DisputeRollup>,
    var hits: Int,
    var misses: Int,
) : DisputeRollupStore {
    override fun find(code: String): DisputeRollup? {
        val hit = (cache.get(code) as DisputeRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeRollup> = inner.list()
}

fun newCachedDisputeRollupStore(inner: DisputeRollupStore, ttl: Int, clock: Clock): DisputeRollupStore {
    return CachedDisputeRollupStore(
        inner = inner,
        cache = newTtlCache<DisputeRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute rollups. Empty fields match all. */
data class DisputeRollupQuery(
    var region: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeRollup>): MutableList<DisputeRollup> {
        val matched = filter(items) { candidate: DisputeRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeRollups(store: DisputeRollupStore, query: DisputeRollupQuery): MutableList<DisputeRollup> {
    return query.apply(store.list())
}
