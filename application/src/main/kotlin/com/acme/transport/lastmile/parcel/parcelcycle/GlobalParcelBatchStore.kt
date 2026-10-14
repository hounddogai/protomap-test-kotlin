package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global parcel batches by code. */
interface GlobalParcelBatchStore {
    fun find(code: String): GlobalParcelBatch?
    fun save(item: GlobalParcelBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalParcelBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalParcelBatchStore(
    var items: MutableMap<String, GlobalParcelBatch>,
    var order: MutableList<String>,
) : GlobalParcelBatchStore {
    override fun find(code: String): GlobalParcelBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalParcelBatch) {
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

    override fun list(): MutableList<GlobalParcelBatch> {
        val out = mutableListOf<GlobalParcelBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalParcelBatchStore(): GlobalParcelBatchStore {
    return MemoryGlobalParcelBatchStore(mutableMapOf<String, GlobalParcelBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalParcelBatchStore(
    var inner: GlobalParcelBatchStore,
    var cache: TtlCache<GlobalParcelBatch>,
    var hits: Int,
    var misses: Int,
) : GlobalParcelBatchStore {
    override fun find(code: String): GlobalParcelBatch? {
        val hit = (cache.get(code) as GlobalParcelBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalParcelBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalParcelBatch> = inner.list()
}

fun newCachedGlobalParcelBatchStore(inner: GlobalParcelBatchStore, ttl: Int, clock: Clock): GlobalParcelBatchStore {
    return CachedGlobalParcelBatchStore(
        inner = inner,
        cache = newTtlCache<GlobalParcelBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global parcel batches. Empty fields match all. */
data class GlobalParcelBatchQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalParcelBatch): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalParcelBatch>): MutableList<GlobalParcelBatch> {
        val matched = filter(items) { candidate: GlobalParcelBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalParcelBatches(store: GlobalParcelBatchStore, query: GlobalParcelBatchQuery): MutableList<GlobalParcelBatch> {
    return query.apply(store.list())
}
