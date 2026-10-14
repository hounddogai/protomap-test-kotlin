package com.acme.transport.customs.hold.baseline

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold baselines by code. */
interface HoldBaselineStore {
    fun find(code: String): HoldBaseline?
    fun save(item: HoldBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldBaselineStore(
    var items: MutableMap<String, HoldBaseline>,
    var order: MutableList<String>,
) : HoldBaselineStore {
    override fun find(code: String): HoldBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldBaseline) {
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

    override fun list(): MutableList<HoldBaseline> {
        val out = mutableListOf<HoldBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldBaselineStore(): HoldBaselineStore {
    return MemoryHoldBaselineStore(mutableMapOf<String, HoldBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldBaselineStore(
    var inner: HoldBaselineStore,
    var cache: TtlCache<HoldBaseline>,
    var hits: Int,
    var misses: Int,
) : HoldBaselineStore {
    override fun find(code: String): HoldBaseline? {
        val hit = (cache.get(code) as HoldBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldBaseline> = inner.list()
}

fun newCachedHoldBaselineStore(inner: HoldBaselineStore, ttl: Int, clock: Clock): HoldBaselineStore {
    return CachedHoldBaselineStore(
        inner = inner,
        cache = newTtlCache<HoldBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold baselines. Empty fields match all. */
data class HoldBaselineQuery(
    var source: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldBaseline): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldBaseline>): MutableList<HoldBaseline> {
        val matched = filter(items) { candidate: HoldBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldBaselines(store: HoldBaselineStore, query: HoldBaselineQuery): MutableList<HoldBaseline> {
    return query.apply(store.list())
}
