package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary scorecard thresholds by code. */
interface PrimaryScorecardThresholdStore {
    fun find(code: String): PrimaryScorecardThreshold?
    fun save(item: PrimaryScorecardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryScorecardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryScorecardThresholdStore(
    var items: MutableMap<String, PrimaryScorecardThreshold>,
    var order: MutableList<String>,
) : PrimaryScorecardThresholdStore {
    override fun find(code: String): PrimaryScorecardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryScorecardThreshold) {
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

    override fun list(): MutableList<PrimaryScorecardThreshold> {
        val out = mutableListOf<PrimaryScorecardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryScorecardThresholdStore(): PrimaryScorecardThresholdStore {
    return MemoryPrimaryScorecardThresholdStore(
        mutableMapOf<String, PrimaryScorecardThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryScorecardThresholdStore(
    var inner: PrimaryScorecardThresholdStore,
    var cache: TtlCache<PrimaryScorecardThreshold>,
    var hits: Int,
    var misses: Int,
) : PrimaryScorecardThresholdStore {
    override fun find(code: String): PrimaryScorecardThreshold? {
        val hit = (cache.get(code) as PrimaryScorecardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryScorecardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryScorecardThreshold> = inner.list()
}

fun newCachedPrimaryScorecardThresholdStore(inner: PrimaryScorecardThresholdStore, ttl: Int, clock: Clock): PrimaryScorecardThresholdStore {
    return CachedPrimaryScorecardThresholdStore(
        inner = inner,
        cache = newTtlCache<PrimaryScorecardThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary scorecard thresholds. Empty fields match all. */
data class PrimaryScorecardThresholdQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryScorecardThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryScorecardThreshold>): MutableList<PrimaryScorecardThreshold> {
        val matched = filter(items) { candidate: PrimaryScorecardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryScorecardThresholds(store: PrimaryScorecardThresholdStore, query: PrimaryScorecardThresholdQuery): MutableList<PrimaryScorecardThreshold> {
    return query.apply(store.list())
}
