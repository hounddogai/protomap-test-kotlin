package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period scores by code. */
interface DutyPeriodScoreStore {
    fun find(code: String): DutyPeriodScore?
    fun save(item: DutyPeriodScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodScoreStore(
    var items: MutableMap<String, DutyPeriodScore>,
    var order: MutableList<String>,
) : DutyPeriodScoreStore {
    override fun find(code: String): DutyPeriodScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodScore) {
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

    override fun list(): MutableList<DutyPeriodScore> {
        val out = mutableListOf<DutyPeriodScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodScoreStore(): DutyPeriodScoreStore {
    return MemoryDutyPeriodScoreStore(mutableMapOf<String, DutyPeriodScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodScoreStore(
    var inner: DutyPeriodScoreStore,
    var cache: TtlCache<DutyPeriodScore>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodScoreStore {
    override fun find(code: String): DutyPeriodScore? {
        val hit = (cache.get(code) as DutyPeriodScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodScore> = inner.list()
}

fun newCachedDutyPeriodScoreStore(inner: DutyPeriodScoreStore, ttl: Int, clock: Clock): DutyPeriodScoreStore {
    return CachedDutyPeriodScoreStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period scores. Empty fields match all. */
data class DutyPeriodScoreQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodScore>): MutableList<DutyPeriodScore> {
        val matched = filter(items) { candidate: DutyPeriodScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodScores(store: DutyPeriodScoreStore, query: DutyPeriodScoreQuery): MutableList<DutyPeriodScore> {
    return query.apply(store.list())
}
