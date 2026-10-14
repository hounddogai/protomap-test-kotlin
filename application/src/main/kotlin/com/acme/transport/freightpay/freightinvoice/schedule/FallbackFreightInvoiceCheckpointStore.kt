package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback freight invoice checkpoints by code. */
interface FallbackFreightInvoiceCheckpointStore {
    fun find(code: String): FallbackFreightInvoiceCheckpoint?
    fun save(item: FallbackFreightInvoiceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackFreightInvoiceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackFreightInvoiceCheckpointStore(
    var items: MutableMap<String, FallbackFreightInvoiceCheckpoint>,
    var order: MutableList<String>,
) : FallbackFreightInvoiceCheckpointStore {
    override fun find(code: String): FallbackFreightInvoiceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackFreightInvoiceCheckpoint) {
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

    override fun list(): MutableList<FallbackFreightInvoiceCheckpoint> {
        val out = mutableListOf<FallbackFreightInvoiceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackFreightInvoiceCheckpointStore(): FallbackFreightInvoiceCheckpointStore {
    return MemoryFallbackFreightInvoiceCheckpointStore(
        mutableMapOf<String, FallbackFreightInvoiceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackFreightInvoiceCheckpointStore(
    var inner: FallbackFreightInvoiceCheckpointStore,
    var cache: TtlCache<FallbackFreightInvoiceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : FallbackFreightInvoiceCheckpointStore {
    override fun find(code: String): FallbackFreightInvoiceCheckpoint? {
        val hit = (cache.get(code) as FallbackFreightInvoiceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackFreightInvoiceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackFreightInvoiceCheckpoint> = inner.list()
}

fun newCachedFallbackFreightInvoiceCheckpointStore(inner: FallbackFreightInvoiceCheckpointStore, ttl: Int, clock: Clock): FallbackFreightInvoiceCheckpointStore {
    return CachedFallbackFreightInvoiceCheckpointStore(
        inner = inner,
        cache = newTtlCache<FallbackFreightInvoiceCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback freight invoice checkpoints. Empty fields match all. */
data class FallbackFreightInvoiceCheckpointQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackFreightInvoiceCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackFreightInvoiceCheckpoint>): MutableList<FallbackFreightInvoiceCheckpoint> {
        val matched = filter(items) { candidate: FallbackFreightInvoiceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackFreightInvoiceCheckpoints(store: FallbackFreightInvoiceCheckpointStore, query: FallbackFreightInvoiceCheckpointQuery): MutableList<FallbackFreightInvoiceCheckpoint> {
    return query.apply(store.list())
}
