package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered freight invoice checkpoints by code. */
interface TieredFreightInvoiceCheckpointStore {
    fun find(code: String): TieredFreightInvoiceCheckpoint?
    fun save(item: TieredFreightInvoiceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFreightInvoiceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFreightInvoiceCheckpointStore(
    var items: MutableMap<String, TieredFreightInvoiceCheckpoint>,
    var order: MutableList<String>,
) : TieredFreightInvoiceCheckpointStore {
    override fun find(code: String): TieredFreightInvoiceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFreightInvoiceCheckpoint) {
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

    override fun list(): MutableList<TieredFreightInvoiceCheckpoint> {
        val out = mutableListOf<TieredFreightInvoiceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFreightInvoiceCheckpointStore(): TieredFreightInvoiceCheckpointStore {
    return MemoryTieredFreightInvoiceCheckpointStore(
        mutableMapOf<String, TieredFreightInvoiceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFreightInvoiceCheckpointStore(
    var inner: TieredFreightInvoiceCheckpointStore,
    var cache: TtlCache<TieredFreightInvoiceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TieredFreightInvoiceCheckpointStore {
    override fun find(code: String): TieredFreightInvoiceCheckpoint? {
        val hit = (cache.get(code) as TieredFreightInvoiceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFreightInvoiceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFreightInvoiceCheckpoint> = inner.list()
}

fun newCachedTieredFreightInvoiceCheckpointStore(inner: TieredFreightInvoiceCheckpointStore, ttl: Int, clock: Clock): TieredFreightInvoiceCheckpointStore {
    return CachedTieredFreightInvoiceCheckpointStore(
        inner = inner,
        cache = newTtlCache<TieredFreightInvoiceCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered freight invoice checkpoints. Empty fields match all. */
data class TieredFreightInvoiceCheckpointQuery(
    var depot: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFreightInvoiceCheckpoint): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredFreightInvoiceCheckpoint>): MutableList<TieredFreightInvoiceCheckpoint> {
        val matched = filter(items) { candidate: TieredFreightInvoiceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFreightInvoiceCheckpoints(store: TieredFreightInvoiceCheckpointStore, query: TieredFreightInvoiceCheckpointQuery): MutableList<TieredFreightInvoiceCheckpoint> {
    return query.apply(store.list())
}
