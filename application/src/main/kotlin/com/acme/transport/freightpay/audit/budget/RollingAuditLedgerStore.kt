package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling audit ledgers by code. */
interface RollingAuditLedgerStore {
    fun find(code: String): RollingAuditLedger?
    fun save(item: RollingAuditLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAuditLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAuditLedgerStore(
    var items: MutableMap<String, RollingAuditLedger>,
    var order: MutableList<String>,
) : RollingAuditLedgerStore {
    override fun find(code: String): RollingAuditLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAuditLedger) {
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

    override fun list(): MutableList<RollingAuditLedger> {
        val out = mutableListOf<RollingAuditLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAuditLedgerStore(): RollingAuditLedgerStore {
    return MemoryRollingAuditLedgerStore(mutableMapOf<String, RollingAuditLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAuditLedgerStore(
    var inner: RollingAuditLedgerStore,
    var cache: TtlCache<RollingAuditLedger>,
    var hits: Int,
    var misses: Int,
) : RollingAuditLedgerStore {
    override fun find(code: String): RollingAuditLedger? {
        val hit = (cache.get(code) as RollingAuditLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAuditLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAuditLedger> = inner.list()
}

fun newCachedRollingAuditLedgerStore(inner: RollingAuditLedgerStore, ttl: Int, clock: Clock): RollingAuditLedgerStore {
    return CachedRollingAuditLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingAuditLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling audit ledgers. Empty fields match all. */
data class RollingAuditLedgerQuery(
    var source: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAuditLedger): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RollingAuditLedger>): MutableList<RollingAuditLedger> {
        val matched = filter(items) { candidate: RollingAuditLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAuditLedgers(store: RollingAuditLedgerStore, query: RollingAuditLedgerQuery): MutableList<RollingAuditLedger> {
    return query.apply(store.list())
}
