package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending violation ledgers by code. */
interface PendingViolationLedgerStore {
    fun find(code: String): PendingViolationLedger?
    fun save(item: PendingViolationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingViolationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingViolationLedgerStore(
    var items: MutableMap<String, PendingViolationLedger>,
    var order: MutableList<String>,
) : PendingViolationLedgerStore {
    override fun find(code: String): PendingViolationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingViolationLedger) {
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

    override fun list(): MutableList<PendingViolationLedger> {
        val out = mutableListOf<PendingViolationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingViolationLedgerStore(): PendingViolationLedgerStore {
    return MemoryPendingViolationLedgerStore(
        mutableMapOf<String, PendingViolationLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingViolationLedgerStore(
    var inner: PendingViolationLedgerStore,
    var cache: TtlCache<PendingViolationLedger>,
    var hits: Int,
    var misses: Int,
) : PendingViolationLedgerStore {
    override fun find(code: String): PendingViolationLedger? {
        val hit = (cache.get(code) as PendingViolationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingViolationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingViolationLedger> = inner.list()
}

fun newCachedPendingViolationLedgerStore(inner: PendingViolationLedgerStore, ttl: Int, clock: Clock): PendingViolationLedgerStore {
    return CachedPendingViolationLedgerStore(
        inner = inner,
        cache = newTtlCache<PendingViolationLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending violation ledgers. Empty fields match all. */
data class PendingViolationLedgerQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingViolationLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingViolationLedger>): MutableList<PendingViolationLedger> {
        val matched = filter(items) { candidate: PendingViolationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingViolationLedgers(store: PendingViolationLedgerStore, query: PendingViolationLedgerQuery): MutableList<PendingViolationLedger> {
    return query.apply(store.list())
}
