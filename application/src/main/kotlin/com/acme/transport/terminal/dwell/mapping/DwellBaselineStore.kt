package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell baselines by code. */
interface DwellBaselineStore {
    fun find(code: String): DwellBaseline?
    fun save(item: DwellBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellBaselineStore(
    var items: MutableMap<String, DwellBaseline>,
    var order: MutableList<String>,
) : DwellBaselineStore {
    override fun find(code: String): DwellBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellBaseline) {
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

    override fun list(): MutableList<DwellBaseline> {
        val out = mutableListOf<DwellBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellBaselineStore(): DwellBaselineStore {
    return MemoryDwellBaselineStore(mutableMapOf<String, DwellBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellBaselineStore(
    var inner: DwellBaselineStore,
    var cache: TtlCache<DwellBaseline>,
    var hits: Int,
    var misses: Int,
) : DwellBaselineStore {
    override fun find(code: String): DwellBaseline? {
        val hit = (cache.get(code) as DwellBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellBaseline> = inner.list()
}

fun newCachedDwellBaselineStore(inner: DwellBaselineStore, ttl: Int, clock: Clock): DwellBaselineStore {
    return CachedDwellBaselineStore(
        inner = inner,
        cache = newTtlCache<DwellBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell baselines. Empty fields match all. */
data class DwellBaselineQuery(
    var depot: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellBaseline): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellBaseline>): MutableList<DwellBaseline> {
        val matched = filter(items) { candidate: DwellBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellBaselines(store: DwellBaselineStore, query: DwellBaselineQuery): MutableList<DwellBaseline> {
    return query.apply(store.list())
}
