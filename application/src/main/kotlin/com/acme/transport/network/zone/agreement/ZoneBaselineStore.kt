package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone baselines by code. */
interface ZoneBaselineStore {
    fun find(code: String): ZoneBaseline?
    fun save(item: ZoneBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneBaselineStore(
    var items: MutableMap<String, ZoneBaseline>,
    var order: MutableList<String>,
) : ZoneBaselineStore {
    override fun find(code: String): ZoneBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneBaseline) {
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

    override fun list(): MutableList<ZoneBaseline> {
        val out = mutableListOf<ZoneBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneBaselineStore(): ZoneBaselineStore {
    return MemoryZoneBaselineStore(mutableMapOf<String, ZoneBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneBaselineStore(
    var inner: ZoneBaselineStore,
    var cache: TtlCache<ZoneBaseline>,
    var hits: Int,
    var misses: Int,
) : ZoneBaselineStore {
    override fun find(code: String): ZoneBaseline? {
        val hit = (cache.get(code) as ZoneBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneBaseline> = inner.list()
}

fun newCachedZoneBaselineStore(inner: ZoneBaselineStore, ttl: Int, clock: Clock): ZoneBaselineStore {
    return CachedZoneBaselineStore(
        inner = inner,
        cache = newTtlCache<ZoneBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone baselines. Empty fields match all. */
data class ZoneBaselineQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneBaseline): Boolean {
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

    fun apply(items: MutableList<ZoneBaseline>): MutableList<ZoneBaseline> {
        val matched = filter(items) { candidate: ZoneBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneBaselines(store: ZoneBaselineStore, query: ZoneBaselineQuery): MutableList<ZoneBaseline> {
    return query.apply(store.list())
}
