package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice checkpoints by code. */
interface FreightInvoiceCheckpointStore {
    fun find(code: String): FreightInvoiceCheckpoint?
    fun save(item: FreightInvoiceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceCheckpointStore(
    var items: MutableMap<String, FreightInvoiceCheckpoint>,
    var order: MutableList<String>,
) : FreightInvoiceCheckpointStore {
    override fun find(code: String): FreightInvoiceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceCheckpoint) {
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

    override fun list(): MutableList<FreightInvoiceCheckpoint> {
        val out = mutableListOf<FreightInvoiceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceCheckpointStore(): FreightInvoiceCheckpointStore {
    return MemoryFreightInvoiceCheckpointStore(
        mutableMapOf<String, FreightInvoiceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceCheckpointStore(
    var inner: FreightInvoiceCheckpointStore,
    var cache: TtlCache<FreightInvoiceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceCheckpointStore {
    override fun find(code: String): FreightInvoiceCheckpoint? {
        val hit = (cache.get(code) as FreightInvoiceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceCheckpoint> = inner.list()
}

fun newCachedFreightInvoiceCheckpointStore(inner: FreightInvoiceCheckpointStore, ttl: Int, clock: Clock): FreightInvoiceCheckpointStore {
    return CachedFreightInvoiceCheckpointStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice checkpoints. Empty fields match all. */
data class FreightInvoiceCheckpointQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<FreightInvoiceCheckpoint>): MutableList<FreightInvoiceCheckpoint> {
        val matched = filter(items) { candidate: FreightInvoiceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceCheckpoints(store: FreightInvoiceCheckpointStore, query: FreightInvoiceCheckpointQuery): MutableList<FreightInvoiceCheckpoint> {
    return query.apply(store.list())
}
