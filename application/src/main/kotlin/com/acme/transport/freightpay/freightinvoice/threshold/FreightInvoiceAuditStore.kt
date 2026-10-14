package com.acme.transport.freightpay.freightinvoice.threshold

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice audits by code. */
interface FreightInvoiceAuditStore {
    fun find(code: String): FreightInvoiceAudit?
    fun save(item: FreightInvoiceAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceAuditStore(
    var items: MutableMap<String, FreightInvoiceAudit>,
    var order: MutableList<String>,
) : FreightInvoiceAuditStore {
    override fun find(code: String): FreightInvoiceAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceAudit) {
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

    override fun list(): MutableList<FreightInvoiceAudit> {
        val out = mutableListOf<FreightInvoiceAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceAuditStore(): FreightInvoiceAuditStore {
    return MemoryFreightInvoiceAuditStore(mutableMapOf<String, FreightInvoiceAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceAuditStore(
    var inner: FreightInvoiceAuditStore,
    var cache: TtlCache<FreightInvoiceAudit>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceAuditStore {
    override fun find(code: String): FreightInvoiceAudit? {
        val hit = (cache.get(code) as FreightInvoiceAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceAudit> = inner.list()
}

fun newCachedFreightInvoiceAuditStore(inner: FreightInvoiceAuditStore, ttl: Int, clock: Clock): FreightInvoiceAuditStore {
    return CachedFreightInvoiceAuditStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice audits. Empty fields match all. */
data class FreightInvoiceAuditQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceAudit): Boolean {
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

    fun apply(items: MutableList<FreightInvoiceAudit>): MutableList<FreightInvoiceAudit> {
        val matched = filter(items) { candidate: FreightInvoiceAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceAudits(store: FreightInvoiceAuditStore, query: FreightInvoiceAuditQuery): MutableList<FreightInvoiceAudit> {
    return query.apply(store.list())
}
