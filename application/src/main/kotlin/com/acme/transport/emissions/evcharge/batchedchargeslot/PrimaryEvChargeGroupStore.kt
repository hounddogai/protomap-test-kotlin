package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary ev charge groups by code. */
interface PrimaryEvChargeGroupStore {
    fun find(code: String): PrimaryEvChargeGroup?
    fun save(item: PrimaryEvChargeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryEvChargeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryEvChargeGroupStore(
    var items: MutableMap<String, PrimaryEvChargeGroup>,
    var order: MutableList<String>,
) : PrimaryEvChargeGroupStore {
    override fun find(code: String): PrimaryEvChargeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryEvChargeGroup) {
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

    override fun list(): MutableList<PrimaryEvChargeGroup> {
        val out = mutableListOf<PrimaryEvChargeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryEvChargeGroupStore(): PrimaryEvChargeGroupStore {
    return MemoryPrimaryEvChargeGroupStore(
        mutableMapOf<String, PrimaryEvChargeGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryEvChargeGroupStore(
    var inner: PrimaryEvChargeGroupStore,
    var cache: TtlCache<PrimaryEvChargeGroup>,
    var hits: Int,
    var misses: Int,
) : PrimaryEvChargeGroupStore {
    override fun find(code: String): PrimaryEvChargeGroup? {
        val hit = (cache.get(code) as PrimaryEvChargeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryEvChargeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryEvChargeGroup> = inner.list()
}

fun newCachedPrimaryEvChargeGroupStore(inner: PrimaryEvChargeGroupStore, ttl: Int, clock: Clock): PrimaryEvChargeGroupStore {
    return CachedPrimaryEvChargeGroupStore(
        inner = inner,
        cache = newTtlCache<PrimaryEvChargeGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary ev charge groups. Empty fields match all. */
data class PrimaryEvChargeGroupQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryEvChargeGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryEvChargeGroup>): MutableList<PrimaryEvChargeGroup> {
        val matched = filter(items) { candidate: PrimaryEvChargeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryEvChargeGroups(store: PrimaryEvChargeGroupStore, query: PrimaryEvChargeGroupQuery): MutableList<PrimaryEvChargeGroup> {
    return query.apply(store.list())
}
