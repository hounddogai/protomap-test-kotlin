package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered incident baselines by code. */
interface TieredIncidentBaselineStore {
    fun find(code: String): TieredIncidentBaseline?
    fun save(item: TieredIncidentBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredIncidentBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredIncidentBaselineStore(
    var items: MutableMap<String, TieredIncidentBaseline>,
    var order: MutableList<String>,
) : TieredIncidentBaselineStore {
    override fun find(code: String): TieredIncidentBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredIncidentBaseline) {
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

    override fun list(): MutableList<TieredIncidentBaseline> {
        val out = mutableListOf<TieredIncidentBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredIncidentBaselineStore(): TieredIncidentBaselineStore {
    return MemoryTieredIncidentBaselineStore(
        mutableMapOf<String, TieredIncidentBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredIncidentBaselineStore(
    var inner: TieredIncidentBaselineStore,
    var cache: TtlCache<TieredIncidentBaseline>,
    var hits: Int,
    var misses: Int,
) : TieredIncidentBaselineStore {
    override fun find(code: String): TieredIncidentBaseline? {
        val hit = (cache.get(code) as TieredIncidentBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredIncidentBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredIncidentBaseline> = inner.list()
}

fun newCachedTieredIncidentBaselineStore(inner: TieredIncidentBaselineStore, ttl: Int, clock: Clock): TieredIncidentBaselineStore {
    return CachedTieredIncidentBaselineStore(
        inner = inner,
        cache = newTtlCache<TieredIncidentBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered incident baselines. Empty fields match all. */
data class TieredIncidentBaselineQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredIncidentBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredIncidentBaseline>): MutableList<TieredIncidentBaseline> {
        val matched = filter(items) { candidate: TieredIncidentBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredIncidentBaselines(store: TieredIncidentBaselineStore, query: TieredIncidentBaselineQuery): MutableList<TieredIncidentBaseline> {
    return query.apply(store.list())
}
