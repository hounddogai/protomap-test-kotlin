package com.acme.transport.fleetops.tire.batch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional tire batches by code. */
interface RegionalTireBatchStore {
    fun find(code: String): RegionalTireBatch?
    fun save(item: RegionalTireBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTireBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTireBatchStore(
    var items: MutableMap<String, RegionalTireBatch>,
    var order: MutableList<String>,
) : RegionalTireBatchStore {
    override fun find(code: String): RegionalTireBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTireBatch) {
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

    override fun list(): MutableList<RegionalTireBatch> {
        val out = mutableListOf<RegionalTireBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTireBatchStore(): RegionalTireBatchStore {
    return MemoryRegionalTireBatchStore(mutableMapOf<String, RegionalTireBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTireBatchStore(
    var inner: RegionalTireBatchStore,
    var cache: TtlCache<RegionalTireBatch>,
    var hits: Int,
    var misses: Int,
) : RegionalTireBatchStore {
    override fun find(code: String): RegionalTireBatch? {
        val hit = (cache.get(code) as RegionalTireBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTireBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTireBatch> = inner.list()
}

fun newCachedRegionalTireBatchStore(inner: RegionalTireBatchStore, ttl: Int, clock: Clock): RegionalTireBatchStore {
    return CachedRegionalTireBatchStore(
        inner = inner,
        cache = newTtlCache<RegionalTireBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional tire batches. Empty fields match all. */
data class RegionalTireBatchQuery(
    var region: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTireBatch): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTireBatch>): MutableList<RegionalTireBatch> {
        val matched = filter(items) { candidate: RegionalTireBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTireBatches(store: RegionalTireBatchStore, query: RegionalTireBatchQuery): MutableList<RegionalTireBatch> {
    return query.apply(store.list())
}
