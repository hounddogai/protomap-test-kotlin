package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional ev charge scores by code. */
interface RegionalEvChargeScoreStore {
    fun find(code: String): RegionalEvChargeScore?
    fun save(item: RegionalEvChargeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalEvChargeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalEvChargeScoreStore(
    var items: MutableMap<String, RegionalEvChargeScore>,
    var order: MutableList<String>,
) : RegionalEvChargeScoreStore {
    override fun find(code: String): RegionalEvChargeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalEvChargeScore) {
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

    override fun list(): MutableList<RegionalEvChargeScore> {
        val out = mutableListOf<RegionalEvChargeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalEvChargeScoreStore(): RegionalEvChargeScoreStore {
    return MemoryRegionalEvChargeScoreStore(
        mutableMapOf<String, RegionalEvChargeScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalEvChargeScoreStore(
    var inner: RegionalEvChargeScoreStore,
    var cache: TtlCache<RegionalEvChargeScore>,
    var hits: Int,
    var misses: Int,
) : RegionalEvChargeScoreStore {
    override fun find(code: String): RegionalEvChargeScore? {
        val hit = (cache.get(code) as RegionalEvChargeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalEvChargeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalEvChargeScore> = inner.list()
}

fun newCachedRegionalEvChargeScoreStore(inner: RegionalEvChargeScoreStore, ttl: Int, clock: Clock): RegionalEvChargeScoreStore {
    return CachedRegionalEvChargeScoreStore(
        inner = inner,
        cache = newTtlCache<RegionalEvChargeScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional ev charge scores. Empty fields match all. */
data class RegionalEvChargeScoreQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalEvChargeScore): Boolean {
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

    fun apply(items: MutableList<RegionalEvChargeScore>): MutableList<RegionalEvChargeScore> {
        val matched = filter(items) { candidate: RegionalEvChargeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalEvChargeScores(store: RegionalEvChargeScoreStore, query: RegionalEvChargeScoreQuery): MutableList<RegionalEvChargeScore> {
    return query.apply(store.list())
}
