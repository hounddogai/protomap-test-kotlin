package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice snapshots by code. */
interface FreightInvoiceSnapshotStore {
    fun find(code: String): FreightInvoiceSnapshot?
    fun save(item: FreightInvoiceSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceSnapshotStore(
    var items: MutableMap<String, FreightInvoiceSnapshot>,
    var order: MutableList<String>,
) : FreightInvoiceSnapshotStore {
    override fun find(code: String): FreightInvoiceSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceSnapshot) {
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

    override fun list(): MutableList<FreightInvoiceSnapshot> {
        val out = mutableListOf<FreightInvoiceSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceSnapshotStore(): FreightInvoiceSnapshotStore {
    return MemoryFreightInvoiceSnapshotStore(
        mutableMapOf<String, FreightInvoiceSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceSnapshotStore(
    var inner: FreightInvoiceSnapshotStore,
    var cache: TtlCache<FreightInvoiceSnapshot>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceSnapshotStore {
    override fun find(code: String): FreightInvoiceSnapshot? {
        val hit = (cache.get(code) as FreightInvoiceSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceSnapshot> = inner.list()
}

fun newCachedFreightInvoiceSnapshotStore(inner: FreightInvoiceSnapshotStore, ttl: Int, clock: Clock): FreightInvoiceSnapshotStore {
    return CachedFreightInvoiceSnapshotStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice snapshots. Empty fields match all. */
data class FreightInvoiceSnapshotQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceSnapshot>): MutableList<FreightInvoiceSnapshot> {
        val matched = filter(items) { candidate: FreightInvoiceSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceSnapshots(store: FreightInvoiceSnapshotStore, query: FreightInvoiceSnapshotQuery): MutableList<FreightInvoiceSnapshot> {
    return query.apply(store.list())
}
