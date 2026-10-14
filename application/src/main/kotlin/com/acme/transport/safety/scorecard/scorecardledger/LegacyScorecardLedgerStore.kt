package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy scorecard ledgers by code. */
interface LegacyScorecardLedgerStore {
    fun find(code: String): LegacyScorecardLedger?
    fun save(item: LegacyScorecardLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyScorecardLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyScorecardLedgerStore(
    var items: MutableMap<String, LegacyScorecardLedger>,
    var order: MutableList<String>,
) : LegacyScorecardLedgerStore {
    override fun find(code: String): LegacyScorecardLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyScorecardLedger) {
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

    override fun list(): MutableList<LegacyScorecardLedger> {
        val out = mutableListOf<LegacyScorecardLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyScorecardLedgerStore(): LegacyScorecardLedgerStore {
    return MemoryLegacyScorecardLedgerStore(
        mutableMapOf<String, LegacyScorecardLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyScorecardLedgerStore(
    var inner: LegacyScorecardLedgerStore,
    var cache: TtlCache<LegacyScorecardLedger>,
    var hits: Int,
    var misses: Int,
) : LegacyScorecardLedgerStore {
    override fun find(code: String): LegacyScorecardLedger? {
        val hit = (cache.get(code) as LegacyScorecardLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyScorecardLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyScorecardLedger> = inner.list()
}

fun newCachedLegacyScorecardLedgerStore(inner: LegacyScorecardLedgerStore, ttl: Int, clock: Clock): LegacyScorecardLedgerStore {
    return CachedLegacyScorecardLedgerStore(
        inner = inner,
        cache = newTtlCache<LegacyScorecardLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy scorecard ledgers. Empty fields match all. */
data class LegacyScorecardLedgerQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyScorecardLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyScorecardLedger>): MutableList<LegacyScorecardLedger> {
        val matched = filter(items) { candidate: LegacyScorecardLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyScorecardLedgers(store: LegacyScorecardLedgerStore, query: LegacyScorecardLedgerQuery): MutableList<LegacyScorecardLedger> {
    return query.apply(store.list())
}
