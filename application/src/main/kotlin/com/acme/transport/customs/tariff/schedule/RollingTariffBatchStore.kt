package com.acme.transport.customs.tariff.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling tariff batches by code. */
interface RollingTariffBatchStore {
    fun find(code: String): RollingTariffBatch?
    fun save(item: RollingTariffBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTariffBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTariffBatchStore(
    var items: MutableMap<String, RollingTariffBatch>,
    var order: MutableList<String>,
) : RollingTariffBatchStore {
    override fun find(code: String): RollingTariffBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTariffBatch) {
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

    override fun list(): MutableList<RollingTariffBatch> {
        val out = mutableListOf<RollingTariffBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTariffBatchStore(): RollingTariffBatchStore {
    return MemoryRollingTariffBatchStore(mutableMapOf<String, RollingTariffBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTariffBatchStore(
    var inner: RollingTariffBatchStore,
    var cache: TtlCache<RollingTariffBatch>,
    var hits: Int,
    var misses: Int,
) : RollingTariffBatchStore {
    override fun find(code: String): RollingTariffBatch? {
        val hit = (cache.get(code) as RollingTariffBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTariffBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTariffBatch> = inner.list()
}

fun newCachedRollingTariffBatchStore(inner: RollingTariffBatchStore, ttl: Int, clock: Clock): RollingTariffBatchStore {
    return CachedRollingTariffBatchStore(
        inner = inner,
        cache = newTtlCache<RollingTariffBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tariff batches. Empty fields match all. */
data class RollingTariffBatchQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTariffBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTariffBatch>): MutableList<RollingTariffBatch> {
        val matched = filter(items) { candidate: RollingTariffBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTariffBatches(store: RollingTariffBatchStore, query: RollingTariffBatchQuery): MutableList<RollingTariffBatch> {
    return query.apply(store.list())
}
