package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell estimates by code. */
interface DwellEstimateStore {
    fun find(code: String): DwellEstimate?
    fun save(item: DwellEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellEstimateStore(
    var items: MutableMap<String, DwellEstimate>,
    var order: MutableList<String>,
) : DwellEstimateStore {
    override fun find(code: String): DwellEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellEstimate) {
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

    override fun list(): MutableList<DwellEstimate> {
        val out = mutableListOf<DwellEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellEstimateStore(): DwellEstimateStore {
    return MemoryDwellEstimateStore(mutableMapOf<String, DwellEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellEstimateStore(
    var inner: DwellEstimateStore,
    var cache: TtlCache<DwellEstimate>,
    var hits: Int,
    var misses: Int,
) : DwellEstimateStore {
    override fun find(code: String): DwellEstimate? {
        val hit = (cache.get(code) as DwellEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellEstimate> = inner.list()
}

fun newCachedDwellEstimateStore(inner: DwellEstimateStore, ttl: Int, clock: Clock): DwellEstimateStore {
    return CachedDwellEstimateStore(
        inner = inner,
        cache = newTtlCache<DwellEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell estimates. Empty fields match all. */
data class DwellEstimateQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellEstimate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellEstimate>): MutableList<DwellEstimate> {
        val matched = filter(items) { candidate: DwellEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellEstimates(store: DwellEstimateStore, query: DwellEstimateQuery): MutableList<DwellEstimate> {
    return query.apply(store.list())
}
