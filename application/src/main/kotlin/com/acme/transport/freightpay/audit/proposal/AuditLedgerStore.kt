package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit ledgers by code. */
interface AuditLedgerStore {
    fun find(code: String): AuditLedger?
    fun save(item: AuditLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditLedgerStore(
    var items: MutableMap<String, AuditLedger>,
    var order: MutableList<String>,
) : AuditLedgerStore {
    override fun find(code: String): AuditLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditLedger) {
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

    override fun list(): MutableList<AuditLedger> {
        val out = mutableListOf<AuditLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditLedgerStore(): AuditLedgerStore {
    return MemoryAuditLedgerStore(mutableMapOf<String, AuditLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditLedgerStore(
    var inner: AuditLedgerStore,
    var cache: TtlCache<AuditLedger>,
    var hits: Int,
    var misses: Int,
) : AuditLedgerStore {
    override fun find(code: String): AuditLedger? {
        val hit = (cache.get(code) as AuditLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditLedger> = inner.list()
}

fun newCachedAuditLedgerStore(inner: AuditLedgerStore, ttl: Int, clock: Clock): AuditLedgerStore {
    return CachedAuditLedgerStore(
        inner = inner,
        cache = newTtlCache<AuditLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit ledgers. Empty fields match all. */
data class AuditLedgerQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditLedger): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditLedger>): MutableList<AuditLedger> {
        val matched = filter(items) { candidate: AuditLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditLedgers(store: AuditLedgerStore, query: AuditLedgerQuery): MutableList<AuditLedger> {
    return query.apply(store.list())
}
