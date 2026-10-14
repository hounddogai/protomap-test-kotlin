package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict dispute groups by code. */
interface StrictDisputeGroupStore {
    fun find(code: String): StrictDisputeGroup?
    fun save(item: StrictDisputeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDisputeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDisputeGroupStore(
    var items: MutableMap<String, StrictDisputeGroup>,
    var order: MutableList<String>,
) : StrictDisputeGroupStore {
    override fun find(code: String): StrictDisputeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDisputeGroup) {
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

    override fun list(): MutableList<StrictDisputeGroup> {
        val out = mutableListOf<StrictDisputeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDisputeGroupStore(): StrictDisputeGroupStore {
    return MemoryStrictDisputeGroupStore(mutableMapOf<String, StrictDisputeGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDisputeGroupStore(
    var inner: StrictDisputeGroupStore,
    var cache: TtlCache<StrictDisputeGroup>,
    var hits: Int,
    var misses: Int,
) : StrictDisputeGroupStore {
    override fun find(code: String): StrictDisputeGroup? {
        val hit = (cache.get(code) as StrictDisputeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDisputeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDisputeGroup> = inner.list()
}

fun newCachedStrictDisputeGroupStore(inner: StrictDisputeGroupStore, ttl: Int, clock: Clock): StrictDisputeGroupStore {
    return CachedStrictDisputeGroupStore(
        inner = inner,
        cache = newTtlCache<StrictDisputeGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dispute groups. Empty fields match all. */
data class StrictDisputeGroupQuery(
    var lane: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDisputeGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDisputeGroup>): MutableList<StrictDisputeGroup> {
        val matched = filter(items) { candidate: StrictDisputeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDisputeGroups(store: StrictDisputeGroupStore, query: StrictDisputeGroupQuery): MutableList<StrictDisputeGroup> {
    return query.apply(store.list())
}
