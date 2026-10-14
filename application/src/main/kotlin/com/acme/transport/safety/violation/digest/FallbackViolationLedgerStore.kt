package com.acme.transport.safety.violation.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback violation ledgers by code. */
interface FallbackViolationLedgerStore {
    fun find(code: String): FallbackViolationLedger?
    fun save(item: FallbackViolationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackViolationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackViolationLedgerStore(
    var items: MutableMap<String, FallbackViolationLedger>,
    var order: MutableList<String>,
) : FallbackViolationLedgerStore {
    override fun find(code: String): FallbackViolationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackViolationLedger) {
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

    override fun list(): MutableList<FallbackViolationLedger> {
        val out = mutableListOf<FallbackViolationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackViolationLedgerStore(): FallbackViolationLedgerStore {
    return MemoryFallbackViolationLedgerStore(
        mutableMapOf<String, FallbackViolationLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackViolationLedgerStore(
    var inner: FallbackViolationLedgerStore,
    var cache: TtlCache<FallbackViolationLedger>,
    var hits: Int,
    var misses: Int,
) : FallbackViolationLedgerStore {
    override fun find(code: String): FallbackViolationLedger? {
        val hit = (cache.get(code) as FallbackViolationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackViolationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackViolationLedger> = inner.list()
}

fun newCachedFallbackViolationLedgerStore(inner: FallbackViolationLedgerStore, ttl: Int, clock: Clock): FallbackViolationLedgerStore {
    return CachedFallbackViolationLedgerStore(
        inner = inner,
        cache = newTtlCache<FallbackViolationLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback violation ledgers. Empty fields match all. */
data class FallbackViolationLedgerQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackViolationLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackViolationLedger>): MutableList<FallbackViolationLedger> {
        val matched = filter(items) { candidate: FallbackViolationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackViolationLedgers(store: FallbackViolationLedgerStore, query: FallbackViolationLedgerQuery): MutableList<FallbackViolationLedger> {
    return query.apply(store.list())
}
