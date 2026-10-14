package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global locker batches by code. */
interface GlobalLockerBatchStore {
    fun find(code: String): GlobalLockerBatch?
    fun save(item: GlobalLockerBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLockerBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLockerBatchStore(
    var items: MutableMap<String, GlobalLockerBatch>,
    var order: MutableList<String>,
) : GlobalLockerBatchStore {
    override fun find(code: String): GlobalLockerBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLockerBatch) {
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

    override fun list(): MutableList<GlobalLockerBatch> {
        val out = mutableListOf<GlobalLockerBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLockerBatchStore(): GlobalLockerBatchStore {
    return MemoryGlobalLockerBatchStore(mutableMapOf<String, GlobalLockerBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLockerBatchStore(
    var inner: GlobalLockerBatchStore,
    var cache: TtlCache<GlobalLockerBatch>,
    var hits: Int,
    var misses: Int,
) : GlobalLockerBatchStore {
    override fun find(code: String): GlobalLockerBatch? {
        val hit = (cache.get(code) as GlobalLockerBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLockerBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLockerBatch> = inner.list()
}

fun newCachedGlobalLockerBatchStore(inner: GlobalLockerBatchStore, ttl: Int, clock: Clock): GlobalLockerBatchStore {
    return CachedGlobalLockerBatchStore(
        inner = inner,
        cache = newTtlCache<GlobalLockerBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global locker batches. Empty fields match all. */
data class GlobalLockerBatchQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLockerBatch): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<GlobalLockerBatch>): MutableList<GlobalLockerBatch> {
        val matched = filter(items) { candidate: GlobalLockerBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLockerBatches(store: GlobalLockerBatchStore, query: GlobalLockerBatchQuery): MutableList<GlobalLockerBatch> {
    return query.apply(store.list())
}
