package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling ledgers by code. */
interface IdlingLedgerStore {
    fun find(code: String): IdlingLedger?
    fun save(item: IdlingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingLedgerStore(
    var items: MutableMap<String, IdlingLedger>,
    var order: MutableList<String>,
) : IdlingLedgerStore {
    override fun find(code: String): IdlingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingLedger) {
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

    override fun list(): MutableList<IdlingLedger> {
        val out = mutableListOf<IdlingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingLedgerStore(): IdlingLedgerStore {
    return MemoryIdlingLedgerStore(mutableMapOf<String, IdlingLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingLedgerStore(
    var inner: IdlingLedgerStore,
    var cache: TtlCache<IdlingLedger>,
    var hits: Int,
    var misses: Int,
) : IdlingLedgerStore {
    override fun find(code: String): IdlingLedger? {
        val hit = (cache.get(code) as IdlingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingLedger> = inner.list()
}

fun newCachedIdlingLedgerStore(inner: IdlingLedgerStore, ttl: Int, clock: Clock): IdlingLedgerStore {
    return CachedIdlingLedgerStore(
        inner = inner,
        cache = newTtlCache<IdlingLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling ledgers. Empty fields match all. */
data class IdlingLedgerQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingLedger): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingLedger>): MutableList<IdlingLedger> {
        val matched = filter(items) { candidate: IdlingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingLedgers(store: IdlingLedgerStore, query: IdlingLedgerQuery): MutableList<IdlingLedger> {
    return query.apply(store.list())
}
