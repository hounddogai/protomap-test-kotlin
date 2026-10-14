package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional service area batches by code. */
interface RegionalServiceAreaBatchStore {
    fun find(code: String): RegionalServiceAreaBatch?
    fun save(item: RegionalServiceAreaBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServiceAreaBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServiceAreaBatchStore(
    var items: MutableMap<String, RegionalServiceAreaBatch>,
    var order: MutableList<String>,
) : RegionalServiceAreaBatchStore {
    override fun find(code: String): RegionalServiceAreaBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServiceAreaBatch) {
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

    override fun list(): MutableList<RegionalServiceAreaBatch> {
        val out = mutableListOf<RegionalServiceAreaBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServiceAreaBatchStore(): RegionalServiceAreaBatchStore {
    return MemoryRegionalServiceAreaBatchStore(
        mutableMapOf<String, RegionalServiceAreaBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServiceAreaBatchStore(
    var inner: RegionalServiceAreaBatchStore,
    var cache: TtlCache<RegionalServiceAreaBatch>,
    var hits: Int,
    var misses: Int,
) : RegionalServiceAreaBatchStore {
    override fun find(code: String): RegionalServiceAreaBatch? {
        val hit = (cache.get(code) as RegionalServiceAreaBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServiceAreaBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServiceAreaBatch> = inner.list()
}

fun newCachedRegionalServiceAreaBatchStore(inner: RegionalServiceAreaBatchStore, ttl: Int, clock: Clock): RegionalServiceAreaBatchStore {
    return CachedRegionalServiceAreaBatchStore(
        inner = inner,
        cache = newTtlCache<RegionalServiceAreaBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service area batches. Empty fields match all. */
data class RegionalServiceAreaBatchQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServiceAreaBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalServiceAreaBatch>): MutableList<RegionalServiceAreaBatch> {
        val matched = filter(items) { candidate: RegionalServiceAreaBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServiceAreaBatches(store: RegionalServiceAreaBatchStore, query: RegionalServiceAreaBatchQuery): MutableList<RegionalServiceAreaBatch> {
    return query.apply(store.list())
}
