package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dwell baselines by code. */
interface CappedDwellBaselineStore {
    fun find(code: String): CappedDwellBaseline?
    fun save(item: CappedDwellBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDwellBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDwellBaselineStore(
    var items: MutableMap<String, CappedDwellBaseline>,
    var order: MutableList<String>,
) : CappedDwellBaselineStore {
    override fun find(code: String): CappedDwellBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDwellBaseline) {
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

    override fun list(): MutableList<CappedDwellBaseline> {
        val out = mutableListOf<CappedDwellBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDwellBaselineStore(): CappedDwellBaselineStore {
    return MemoryCappedDwellBaselineStore(mutableMapOf<String, CappedDwellBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDwellBaselineStore(
    var inner: CappedDwellBaselineStore,
    var cache: TtlCache<CappedDwellBaseline>,
    var hits: Int,
    var misses: Int,
) : CappedDwellBaselineStore {
    override fun find(code: String): CappedDwellBaseline? {
        val hit = (cache.get(code) as CappedDwellBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDwellBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDwellBaseline> = inner.list()
}

fun newCachedCappedDwellBaselineStore(inner: CappedDwellBaselineStore, ttl: Int, clock: Clock): CappedDwellBaselineStore {
    return CachedCappedDwellBaselineStore(
        inner = inner,
        cache = newTtlCache<CappedDwellBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dwell baselines. Empty fields match all. */
data class CappedDwellBaselineQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDwellBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CappedDwellBaseline>): MutableList<CappedDwellBaseline> {
        val matched = filter(items) { candidate: CappedDwellBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDwellBaselines(store: CappedDwellBaselineStore, query: CappedDwellBaselineQuery): MutableList<CappedDwellBaseline> {
    return query.apply(store.list())
}
