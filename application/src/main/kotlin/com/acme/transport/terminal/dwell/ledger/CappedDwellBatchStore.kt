package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dwell batches by code. */
interface CappedDwellBatchStore {
    fun find(code: String): CappedDwellBatch?
    fun save(item: CappedDwellBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDwellBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDwellBatchStore(
    var items: MutableMap<String, CappedDwellBatch>,
    var order: MutableList<String>,
) : CappedDwellBatchStore {
    override fun find(code: String): CappedDwellBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDwellBatch) {
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

    override fun list(): MutableList<CappedDwellBatch> {
        val out = mutableListOf<CappedDwellBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDwellBatchStore(): CappedDwellBatchStore {
    return MemoryCappedDwellBatchStore(mutableMapOf<String, CappedDwellBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDwellBatchStore(
    var inner: CappedDwellBatchStore,
    var cache: TtlCache<CappedDwellBatch>,
    var hits: Int,
    var misses: Int,
) : CappedDwellBatchStore {
    override fun find(code: String): CappedDwellBatch? {
        val hit = (cache.get(code) as CappedDwellBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDwellBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDwellBatch> = inner.list()
}

fun newCachedCappedDwellBatchStore(inner: CappedDwellBatchStore, ttl: Int, clock: Clock): CappedDwellBatchStore {
    return CachedCappedDwellBatchStore(
        inner = inner,
        cache = newTtlCache<CappedDwellBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dwell batches. Empty fields match all. */
data class CappedDwellBatchQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDwellBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDwellBatch>): MutableList<CappedDwellBatch> {
        val matched = filter(items) { candidate: CappedDwellBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDwellBatches(store: CappedDwellBatchStore, query: CappedDwellBatchQuery): MutableList<CappedDwellBatch> {
    return query.apply(store.list())
}
