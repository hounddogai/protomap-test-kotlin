package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient violation ledgers by code. */
interface LenientViolationLedgerStore {
    fun find(code: String): LenientViolationLedger?
    fun save(item: LenientViolationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientViolationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientViolationLedgerStore(
    var items: MutableMap<String, LenientViolationLedger>,
    var order: MutableList<String>,
) : LenientViolationLedgerStore {
    override fun find(code: String): LenientViolationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientViolationLedger) {
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

    override fun list(): MutableList<LenientViolationLedger> {
        val out = mutableListOf<LenientViolationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientViolationLedgerStore(): LenientViolationLedgerStore {
    return MemoryLenientViolationLedgerStore(
        mutableMapOf<String, LenientViolationLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientViolationLedgerStore(
    var inner: LenientViolationLedgerStore,
    var cache: TtlCache<LenientViolationLedger>,
    var hits: Int,
    var misses: Int,
) : LenientViolationLedgerStore {
    override fun find(code: String): LenientViolationLedger? {
        val hit = (cache.get(code) as LenientViolationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientViolationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientViolationLedger> = inner.list()
}

fun newCachedLenientViolationLedgerStore(inner: LenientViolationLedgerStore, ttl: Int, clock: Clock): LenientViolationLedgerStore {
    return CachedLenientViolationLedgerStore(
        inner = inner,
        cache = newTtlCache<LenientViolationLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient violation ledgers. Empty fields match all. */
data class LenientViolationLedgerQuery(
    var country: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientViolationLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LenientViolationLedger>): MutableList<LenientViolationLedger> {
        val matched = filter(items) { candidate: LenientViolationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientViolationLedgers(store: LenientViolationLedgerStore, query: LenientViolationLedgerQuery): MutableList<LenientViolationLedger> {
    return query.apply(store.list())
}
