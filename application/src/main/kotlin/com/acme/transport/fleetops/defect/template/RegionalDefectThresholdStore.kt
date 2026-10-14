package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional defect thresholds by code. */
interface RegionalDefectThresholdStore {
    fun find(code: String): RegionalDefectThreshold?
    fun save(item: RegionalDefectThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDefectThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDefectThresholdStore(
    var items: MutableMap<String, RegionalDefectThreshold>,
    var order: MutableList<String>,
) : RegionalDefectThresholdStore {
    override fun find(code: String): RegionalDefectThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDefectThreshold) {
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

    override fun list(): MutableList<RegionalDefectThreshold> {
        val out = mutableListOf<RegionalDefectThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDefectThresholdStore(): RegionalDefectThresholdStore {
    return MemoryRegionalDefectThresholdStore(
        mutableMapOf<String, RegionalDefectThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDefectThresholdStore(
    var inner: RegionalDefectThresholdStore,
    var cache: TtlCache<RegionalDefectThreshold>,
    var hits: Int,
    var misses: Int,
) : RegionalDefectThresholdStore {
    override fun find(code: String): RegionalDefectThreshold? {
        val hit = (cache.get(code) as RegionalDefectThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDefectThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDefectThreshold> = inner.list()
}

fun newCachedRegionalDefectThresholdStore(inner: RegionalDefectThresholdStore, ttl: Int, clock: Clock): RegionalDefectThresholdStore {
    return CachedRegionalDefectThresholdStore(
        inner = inner,
        cache = newTtlCache<RegionalDefectThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional defect thresholds. Empty fields match all. */
data class RegionalDefectThresholdQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDefectThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RegionalDefectThreshold>): MutableList<RegionalDefectThreshold> {
        val matched = filter(items) { candidate: RegionalDefectThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDefectThresholds(store: RegionalDefectThresholdStore, query: RegionalDefectThresholdQuery): MutableList<RegionalDefectThreshold> {
    return query.apply(store.list())
}
