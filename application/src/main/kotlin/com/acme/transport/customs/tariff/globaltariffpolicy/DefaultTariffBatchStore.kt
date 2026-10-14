package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default tariff batches by code. */
interface DefaultTariffBatchStore {
    fun find(code: String): DefaultTariffBatch?
    fun save(item: DefaultTariffBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTariffBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTariffBatchStore(
    var items: MutableMap<String, DefaultTariffBatch>,
    var order: MutableList<String>,
) : DefaultTariffBatchStore {
    override fun find(code: String): DefaultTariffBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTariffBatch) {
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

    override fun list(): MutableList<DefaultTariffBatch> {
        val out = mutableListOf<DefaultTariffBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTariffBatchStore(): DefaultTariffBatchStore {
    return MemoryDefaultTariffBatchStore(mutableMapOf<String, DefaultTariffBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTariffBatchStore(
    var inner: DefaultTariffBatchStore,
    var cache: TtlCache<DefaultTariffBatch>,
    var hits: Int,
    var misses: Int,
) : DefaultTariffBatchStore {
    override fun find(code: String): DefaultTariffBatch? {
        val hit = (cache.get(code) as DefaultTariffBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTariffBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTariffBatch> = inner.list()
}

fun newCachedDefaultTariffBatchStore(inner: DefaultTariffBatchStore, ttl: Int, clock: Clock): DefaultTariffBatchStore {
    return CachedDefaultTariffBatchStore(
        inner = inner,
        cache = newTtlCache<DefaultTariffBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tariff batches. Empty fields match all. */
data class DefaultTariffBatchQuery(
    var region: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTariffBatch): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<DefaultTariffBatch>): MutableList<DefaultTariffBatch> {
        val matched = filter(items) { candidate: DefaultTariffBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTariffBatches(store: DefaultTariffBatchStore, query: DefaultTariffBatchQuery): MutableList<DefaultTariffBatch> {
    return query.apply(store.list())
}
