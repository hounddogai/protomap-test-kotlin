package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled freight invoice snapshots by code. */
interface ScheduledFreightInvoiceSnapshotStore {
    fun find(code: String): ScheduledFreightInvoiceSnapshot?
    fun save(item: ScheduledFreightInvoiceSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFreightInvoiceSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFreightInvoiceSnapshotStore(
    var items: MutableMap<String, ScheduledFreightInvoiceSnapshot>,
    var order: MutableList<String>,
) : ScheduledFreightInvoiceSnapshotStore {
    override fun find(code: String): ScheduledFreightInvoiceSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFreightInvoiceSnapshot) {
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

    override fun list(): MutableList<ScheduledFreightInvoiceSnapshot> {
        val out = mutableListOf<ScheduledFreightInvoiceSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFreightInvoiceSnapshotStore(): ScheduledFreightInvoiceSnapshotStore {
    return MemoryScheduledFreightInvoiceSnapshotStore(
        mutableMapOf<String, ScheduledFreightInvoiceSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFreightInvoiceSnapshotStore(
    var inner: ScheduledFreightInvoiceSnapshotStore,
    var cache: TtlCache<ScheduledFreightInvoiceSnapshot>,
    var hits: Int,
    var misses: Int,
) : ScheduledFreightInvoiceSnapshotStore {
    override fun find(code: String): ScheduledFreightInvoiceSnapshot? {
        val hit = (cache.get(code) as ScheduledFreightInvoiceSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFreightInvoiceSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFreightInvoiceSnapshot> = inner.list()
}

fun newCachedScheduledFreightInvoiceSnapshotStore(inner: ScheduledFreightInvoiceSnapshotStore, ttl: Int, clock: Clock): ScheduledFreightInvoiceSnapshotStore {
    return CachedScheduledFreightInvoiceSnapshotStore(
        inner = inner,
        cache = newTtlCache<ScheduledFreightInvoiceSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled freight invoice snapshots. Empty fields match all. */
data class ScheduledFreightInvoiceSnapshotQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFreightInvoiceSnapshot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFreightInvoiceSnapshot>): MutableList<ScheduledFreightInvoiceSnapshot> {
        val matched = filter(items) { candidate: ScheduledFreightInvoiceSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFreightInvoiceSnapshots(store: ScheduledFreightInvoiceSnapshotStore, query: ScheduledFreightInvoiceSnapshotQuery): MutableList<ScheduledFreightInvoiceSnapshot> {
    return query.apply(store.list())
}
