package com.acme.transport.emissions.idling.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy idling ledgers by code. */
interface LegacyIdlingLedgerStore {
    fun find(code: String): LegacyIdlingLedger?
    fun save(item: LegacyIdlingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyIdlingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyIdlingLedgerStore(
    var items: MutableMap<String, LegacyIdlingLedger>,
    var order: MutableList<String>,
) : LegacyIdlingLedgerStore {
    override fun find(code: String): LegacyIdlingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyIdlingLedger) {
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

    override fun list(): MutableList<LegacyIdlingLedger> {
        val out = mutableListOf<LegacyIdlingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyIdlingLedgerStore(): LegacyIdlingLedgerStore {
    return MemoryLegacyIdlingLedgerStore(mutableMapOf<String, LegacyIdlingLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyIdlingLedgerStore(
    var inner: LegacyIdlingLedgerStore,
    var cache: TtlCache<LegacyIdlingLedger>,
    var hits: Int,
    var misses: Int,
) : LegacyIdlingLedgerStore {
    override fun find(code: String): LegacyIdlingLedger? {
        val hit = (cache.get(code) as LegacyIdlingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyIdlingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyIdlingLedger> = inner.list()
}

fun newCachedLegacyIdlingLedgerStore(inner: LegacyIdlingLedgerStore, ttl: Int, clock: Clock): LegacyIdlingLedgerStore {
    return CachedLegacyIdlingLedgerStore(
        inner = inner,
        cache = newTtlCache<LegacyIdlingLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy idling ledgers. Empty fields match all. */
data class LegacyIdlingLedgerQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyIdlingLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyIdlingLedger>): MutableList<LegacyIdlingLedger> {
        val matched = filter(items) { candidate: LegacyIdlingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyIdlingLedgers(store: LegacyIdlingLedgerStore, query: LegacyIdlingLedgerQuery): MutableList<LegacyIdlingLedger> {
    return query.apply(store.list())
}
