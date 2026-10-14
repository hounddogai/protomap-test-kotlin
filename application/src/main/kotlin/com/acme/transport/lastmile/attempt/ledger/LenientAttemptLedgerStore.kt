package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient attempt ledgers by code. */
interface LenientAttemptLedgerStore {
    fun find(code: String): LenientAttemptLedger?
    fun save(item: LenientAttemptLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAttemptLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAttemptLedgerStore(
    var items: MutableMap<String, LenientAttemptLedger>,
    var order: MutableList<String>,
) : LenientAttemptLedgerStore {
    override fun find(code: String): LenientAttemptLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAttemptLedger) {
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

    override fun list(): MutableList<LenientAttemptLedger> {
        val out = mutableListOf<LenientAttemptLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAttemptLedgerStore(): LenientAttemptLedgerStore {
    return MemoryLenientAttemptLedgerStore(
        mutableMapOf<String, LenientAttemptLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAttemptLedgerStore(
    var inner: LenientAttemptLedgerStore,
    var cache: TtlCache<LenientAttemptLedger>,
    var hits: Int,
    var misses: Int,
) : LenientAttemptLedgerStore {
    override fun find(code: String): LenientAttemptLedger? {
        val hit = (cache.get(code) as LenientAttemptLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAttemptLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAttemptLedger> = inner.list()
}

fun newCachedLenientAttemptLedgerStore(inner: LenientAttemptLedgerStore, ttl: Int, clock: Clock): LenientAttemptLedgerStore {
    return CachedLenientAttemptLedgerStore(
        inner = inner,
        cache = newTtlCache<LenientAttemptLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient attempt ledgers. Empty fields match all. */
data class LenientAttemptLedgerQuery(
    var channel: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAttemptLedger): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<LenientAttemptLedger>): MutableList<LenientAttemptLedger> {
        val matched = filter(items) { candidate: LenientAttemptLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAttemptLedgers(store: LenientAttemptLedgerStore, query: LenientAttemptLedgerQuery): MutableList<LenientAttemptLedger> {
    return query.apply(store.list())
}
