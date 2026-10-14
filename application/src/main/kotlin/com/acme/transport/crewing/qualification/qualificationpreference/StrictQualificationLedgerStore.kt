package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict qualification ledgers by code. */
interface StrictQualificationLedgerStore {
    fun find(code: String): StrictQualificationLedger?
    fun save(item: StrictQualificationLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictQualificationLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictQualificationLedgerStore(
    var items: MutableMap<String, StrictQualificationLedger>,
    var order: MutableList<String>,
) : StrictQualificationLedgerStore {
    override fun find(code: String): StrictQualificationLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictQualificationLedger) {
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

    override fun list(): MutableList<StrictQualificationLedger> {
        val out = mutableListOf<StrictQualificationLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictQualificationLedgerStore(): StrictQualificationLedgerStore {
    return MemoryStrictQualificationLedgerStore(
        mutableMapOf<String, StrictQualificationLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictQualificationLedgerStore(
    var inner: StrictQualificationLedgerStore,
    var cache: TtlCache<StrictQualificationLedger>,
    var hits: Int,
    var misses: Int,
) : StrictQualificationLedgerStore {
    override fun find(code: String): StrictQualificationLedger? {
        val hit = (cache.get(code) as StrictQualificationLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictQualificationLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictQualificationLedger> = inner.list()
}

fun newCachedStrictQualificationLedgerStore(inner: StrictQualificationLedgerStore, ttl: Int, clock: Clock): StrictQualificationLedgerStore {
    return CachedStrictQualificationLedgerStore(
        inner = inner,
        cache = newTtlCache<StrictQualificationLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict qualification ledgers. Empty fields match all. */
data class StrictQualificationLedgerQuery(
    var segment: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictQualificationLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictQualificationLedger>): MutableList<StrictQualificationLedger> {
        val matched = filter(items) { candidate: StrictQualificationLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictQualificationLedgers(store: StrictQualificationLedgerStore, query: StrictQualificationLedgerQuery): MutableList<StrictQualificationLedger> {
    return query.apply(store.list())
}
