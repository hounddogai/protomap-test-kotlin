package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled sortation ledgers by code. */
interface ScheduledSortationLedgerStore {
    fun find(code: String): ScheduledSortationLedger?
    fun save(item: ScheduledSortationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSortationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSortationLedgerStore(
    var items: MutableMap<String, ScheduledSortationLedger>,
    var order: MutableList<String>,
) : ScheduledSortationLedgerStore {
    override fun find(code: String): ScheduledSortationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSortationLedger) {
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

    override fun list(): MutableList<ScheduledSortationLedger> {
        val out = mutableListOf<ScheduledSortationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSortationLedgerStore(): ScheduledSortationLedgerStore {
    return MemoryScheduledSortationLedgerStore(
        mutableMapOf<String, ScheduledSortationLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSortationLedgerStore(
    var inner: ScheduledSortationLedgerStore,
    var cache: TtlCache<ScheduledSortationLedger>,
    var hits: Int,
    var misses: Int,
) : ScheduledSortationLedgerStore {
    override fun find(code: String): ScheduledSortationLedger? {
        val hit = (cache.get(code) as ScheduledSortationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSortationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSortationLedger> = inner.list()
}

fun newCachedScheduledSortationLedgerStore(inner: ScheduledSortationLedgerStore, ttl: Int, clock: Clock): ScheduledSortationLedgerStore {
    return CachedScheduledSortationLedgerStore(
        inner = inner,
        cache = newTtlCache<ScheduledSortationLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled sortation ledgers. Empty fields match all. */
data class ScheduledSortationLedgerQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSortationLedger): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ScheduledSortationLedger>): MutableList<ScheduledSortationLedger> {
        val matched = filter(items) { candidate: ScheduledSortationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSortationLedgers(store: ScheduledSortationLedgerStore, query: ScheduledSortationLedgerQuery): MutableList<ScheduledSortationLedger> {
    return query.apply(store.list())
}
