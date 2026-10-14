package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard records by code. */
interface ScorecardRecordStore {
    fun find(code: String): ScorecardRecord?
    fun save(item: ScorecardRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardRecordStore(
    var items: MutableMap<String, ScorecardRecord>,
    var order: MutableList<String>,
) : ScorecardRecordStore {
    override fun find(code: String): ScorecardRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardRecord) {
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

    override fun list(): MutableList<ScorecardRecord> {
        val out = mutableListOf<ScorecardRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardRecordStore(): ScorecardRecordStore {
    return MemoryScorecardRecordStore(mutableMapOf<String, ScorecardRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardRecordStore(
    var inner: ScorecardRecordStore,
    var cache: TtlCache<ScorecardRecord>,
    var hits: Int,
    var misses: Int,
) : ScorecardRecordStore {
    override fun find(code: String): ScorecardRecord? {
        val hit = (cache.get(code) as ScorecardRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardRecord> = inner.list()
}

fun newCachedScorecardRecordStore(inner: ScorecardRecordStore, ttl: Int, clock: Clock): ScorecardRecordStore {
    return CachedScorecardRecordStore(
        inner = inner,
        cache = newTtlCache<ScorecardRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard records. Empty fields match all. */
data class ScorecardRecordQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardRecord>): MutableList<ScorecardRecord> {
        val matched = filter(items) { candidate: ScorecardRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardRecords(store: ScorecardRecordStore, query: ScorecardRecordQuery): MutableList<ScorecardRecord> {
    return query.apply(store.list())
}
