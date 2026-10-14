package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual freight invoice checkpoints by code. */
interface ManualFreightInvoiceCheckpointStore {
    fun find(code: String): ManualFreightInvoiceCheckpoint?
    fun save(item: ManualFreightInvoiceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFreightInvoiceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFreightInvoiceCheckpointStore(
    var items: MutableMap<String, ManualFreightInvoiceCheckpoint>,
    var order: MutableList<String>,
) : ManualFreightInvoiceCheckpointStore {
    override fun find(code: String): ManualFreightInvoiceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFreightInvoiceCheckpoint) {
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

    override fun list(): MutableList<ManualFreightInvoiceCheckpoint> {
        val out = mutableListOf<ManualFreightInvoiceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFreightInvoiceCheckpointStore(): ManualFreightInvoiceCheckpointStore {
    return MemoryManualFreightInvoiceCheckpointStore(
        mutableMapOf<String, ManualFreightInvoiceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFreightInvoiceCheckpointStore(
    var inner: ManualFreightInvoiceCheckpointStore,
    var cache: TtlCache<ManualFreightInvoiceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ManualFreightInvoiceCheckpointStore {
    override fun find(code: String): ManualFreightInvoiceCheckpoint? {
        val hit = (cache.get(code) as ManualFreightInvoiceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFreightInvoiceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFreightInvoiceCheckpoint> = inner.list()
}

fun newCachedManualFreightInvoiceCheckpointStore(inner: ManualFreightInvoiceCheckpointStore, ttl: Int, clock: Clock): ManualFreightInvoiceCheckpointStore {
    return CachedManualFreightInvoiceCheckpointStore(
        inner = inner,
        cache = newTtlCache<ManualFreightInvoiceCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual freight invoice checkpoints. Empty fields match all. */
data class ManualFreightInvoiceCheckpointQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFreightInvoiceCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ManualFreightInvoiceCheckpoint>): MutableList<ManualFreightInvoiceCheckpoint> {
        val matched = filter(items) { candidate: ManualFreightInvoiceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFreightInvoiceCheckpoints(store: ManualFreightInvoiceCheckpointStore, query: ManualFreightInvoiceCheckpointQuery): MutableList<ManualFreightInvoiceCheckpoint> {
    return query.apply(store.list())
}
