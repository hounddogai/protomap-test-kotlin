package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped lane plan thresholds by code. */
interface CappedLanePlanThresholdStore {
    fun find(code: String): CappedLanePlanThreshold?
    fun save(item: CappedLanePlanThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLanePlanThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLanePlanThresholdStore(
    var items: MutableMap<String, CappedLanePlanThreshold>,
    var order: MutableList<String>,
) : CappedLanePlanThresholdStore {
    override fun find(code: String): CappedLanePlanThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLanePlanThreshold) {
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

    override fun list(): MutableList<CappedLanePlanThreshold> {
        val out = mutableListOf<CappedLanePlanThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLanePlanThresholdStore(): CappedLanePlanThresholdStore {
    return MemoryCappedLanePlanThresholdStore(
        mutableMapOf<String, CappedLanePlanThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLanePlanThresholdStore(
    var inner: CappedLanePlanThresholdStore,
    var cache: TtlCache<CappedLanePlanThreshold>,
    var hits: Int,
    var misses: Int,
) : CappedLanePlanThresholdStore {
    override fun find(code: String): CappedLanePlanThreshold? {
        val hit = (cache.get(code) as CappedLanePlanThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLanePlanThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLanePlanThreshold> = inner.list()
}

fun newCachedCappedLanePlanThresholdStore(inner: CappedLanePlanThresholdStore, ttl: Int, clock: Clock): CappedLanePlanThresholdStore {
    return CachedCappedLanePlanThresholdStore(
        inner = inner,
        cache = newTtlCache<CappedLanePlanThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped lane plan thresholds. Empty fields match all. */
data class CappedLanePlanThresholdQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLanePlanThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedLanePlanThreshold>): MutableList<CappedLanePlanThreshold> {
        val matched = filter(items) { candidate: CappedLanePlanThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLanePlanThresholds(store: CappedLanePlanThresholdStore, query: CappedLanePlanThresholdQuery): MutableList<CappedLanePlanThreshold> {
    return query.apply(store.list())
}
