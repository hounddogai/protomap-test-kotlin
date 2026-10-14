package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary hold ledgers by code. */
interface PrimaryHoldLedgerStore {
    fun find(code: String): PrimaryHoldLedger?
    fun save(item: PrimaryHoldLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryHoldLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryHoldLedgerStore(
    var items: MutableMap<String, PrimaryHoldLedger>,
    var order: MutableList<String>,
) : PrimaryHoldLedgerStore {
    override fun find(code: String): PrimaryHoldLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryHoldLedger) {
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

    override fun list(): MutableList<PrimaryHoldLedger> {
        val out = mutableListOf<PrimaryHoldLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryHoldLedgerStore(): PrimaryHoldLedgerStore {
    return MemoryPrimaryHoldLedgerStore(mutableMapOf<String, PrimaryHoldLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryHoldLedgerStore(
    var inner: PrimaryHoldLedgerStore,
    var cache: TtlCache<PrimaryHoldLedger>,
    var hits: Int,
    var misses: Int,
) : PrimaryHoldLedgerStore {
    override fun find(code: String): PrimaryHoldLedger? {
        val hit = (cache.get(code) as PrimaryHoldLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryHoldLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryHoldLedger> = inner.list()
}

fun newCachedPrimaryHoldLedgerStore(inner: PrimaryHoldLedgerStore, ttl: Int, clock: Clock): PrimaryHoldLedgerStore {
    return CachedPrimaryHoldLedgerStore(
        inner = inner,
        cache = newTtlCache<PrimaryHoldLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary hold ledgers. Empty fields match all. */
data class PrimaryHoldLedgerQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryHoldLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryHoldLedger>): MutableList<PrimaryHoldLedger> {
        val matched = filter(items) { candidate: PrimaryHoldLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryHoldLedgers(store: PrimaryHoldLedgerStore, query: PrimaryHoldLedgerQuery): MutableList<PrimaryHoldLedger> {
    return query.apply(store.list())
}
