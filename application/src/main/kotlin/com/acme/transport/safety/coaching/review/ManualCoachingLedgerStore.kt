package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual coaching ledgers by code. */
interface ManualCoachingLedgerStore {
    fun find(code: String): ManualCoachingLedger?
    fun save(item: ManualCoachingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCoachingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCoachingLedgerStore(
    var items: MutableMap<String, ManualCoachingLedger>,
    var order: MutableList<String>,
) : ManualCoachingLedgerStore {
    override fun find(code: String): ManualCoachingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCoachingLedger) {
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

    override fun list(): MutableList<ManualCoachingLedger> {
        val out = mutableListOf<ManualCoachingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCoachingLedgerStore(): ManualCoachingLedgerStore {
    return MemoryManualCoachingLedgerStore(
        mutableMapOf<String, ManualCoachingLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCoachingLedgerStore(
    var inner: ManualCoachingLedgerStore,
    var cache: TtlCache<ManualCoachingLedger>,
    var hits: Int,
    var misses: Int,
) : ManualCoachingLedgerStore {
    override fun find(code: String): ManualCoachingLedger? {
        val hit = (cache.get(code) as ManualCoachingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCoachingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCoachingLedger> = inner.list()
}

fun newCachedManualCoachingLedgerStore(inner: ManualCoachingLedgerStore, ttl: Int, clock: Clock): ManualCoachingLedgerStore {
    return CachedManualCoachingLedgerStore(
        inner = inner,
        cache = newTtlCache<ManualCoachingLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual coaching ledgers. Empty fields match all. */
data class ManualCoachingLedgerQuery(
    var segment: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCoachingLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCoachingLedger>): MutableList<ManualCoachingLedger> {
        val matched = filter(items) { candidate: ManualCoachingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCoachingLedgers(store: ManualCoachingLedgerStore, query: ManualCoachingLedgerQuery): MutableList<ManualCoachingLedger> {
    return query.apply(store.list())
}
