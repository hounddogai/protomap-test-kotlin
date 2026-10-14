package com.acme.transport.safety.violation.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation ledgers by code. */
interface ViolationLedgerStore {
    fun find(code: String): ViolationLedger?
    fun save(item: ViolationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationLedgerStore(
    var items: MutableMap<String, ViolationLedger>,
    var order: MutableList<String>,
) : ViolationLedgerStore {
    override fun find(code: String): ViolationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationLedger) {
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

    override fun list(): MutableList<ViolationLedger> {
        val out = mutableListOf<ViolationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationLedgerStore(): ViolationLedgerStore {
    return MemoryViolationLedgerStore(mutableMapOf<String, ViolationLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationLedgerStore(
    var inner: ViolationLedgerStore,
    var cache: TtlCache<ViolationLedger>,
    var hits: Int,
    var misses: Int,
) : ViolationLedgerStore {
    override fun find(code: String): ViolationLedger? {
        val hit = (cache.get(code) as ViolationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationLedger> = inner.list()
}

fun newCachedViolationLedgerStore(inner: ViolationLedgerStore, ttl: Int, clock: Clock): ViolationLedgerStore {
    return CachedViolationLedgerStore(
        inner = inner,
        cache = newTtlCache<ViolationLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation ledgers. Empty fields match all. */
data class ViolationLedgerQuery(
    var country: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationLedger>): MutableList<ViolationLedger> {
        val matched = filter(items) { candidate: ViolationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationLedgers(store: ViolationLedgerStore, query: ViolationLedgerQuery): MutableList<ViolationLedger> {
    return query.apply(store.list())
}
