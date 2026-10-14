package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop ledgers by code. */
interface DropLedgerStore {
    fun find(code: String): DropLedger?
    fun save(item: DropLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropLedgerStore(
    var items: MutableMap<String, DropLedger>,
    var order: MutableList<String>,
) : DropLedgerStore {
    override fun find(code: String): DropLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropLedger) {
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

    override fun list(): MutableList<DropLedger> {
        val out = mutableListOf<DropLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropLedgerStore(): DropLedgerStore {
    return MemoryDropLedgerStore(mutableMapOf<String, DropLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropLedgerStore(
    var inner: DropLedgerStore,
    var cache: TtlCache<DropLedger>,
    var hits: Int,
    var misses: Int,
) : DropLedgerStore {
    override fun find(code: String): DropLedger? {
        val hit = (cache.get(code) as DropLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropLedger> = inner.list()
}

fun newCachedDropLedgerStore(inner: DropLedgerStore, ttl: Int, clock: Clock): DropLedgerStore {
    return CachedDropLedgerStore(
        inner = inner,
        cache = newTtlCache<DropLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop ledgers. Empty fields match all. */
data class DropLedgerQuery(
    var segment: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropLedger>): MutableList<DropLedger> {
        val matched = filter(items) { candidate: DropLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropLedgers(store: DropLedgerStore, query: DropLedgerQuery): MutableList<DropLedger> {
    return query.apply(store.list())
}
