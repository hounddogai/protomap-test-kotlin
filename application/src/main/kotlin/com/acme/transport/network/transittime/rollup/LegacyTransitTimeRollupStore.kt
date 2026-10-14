package com.acme.transport.network.transittime.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy transit time rollups by code. */
interface LegacyTransitTimeRollupStore {
    fun find(code: String): LegacyTransitTimeRollup?
    fun save(item: LegacyTransitTimeRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTransitTimeRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTransitTimeRollupStore(
    var items: MutableMap<String, LegacyTransitTimeRollup>,
    var order: MutableList<String>,
) : LegacyTransitTimeRollupStore {
    override fun find(code: String): LegacyTransitTimeRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTransitTimeRollup) {
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

    override fun list(): MutableList<LegacyTransitTimeRollup> {
        val out = mutableListOf<LegacyTransitTimeRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTransitTimeRollupStore(): LegacyTransitTimeRollupStore {
    return MemoryLegacyTransitTimeRollupStore(
        mutableMapOf<String, LegacyTransitTimeRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTransitTimeRollupStore(
    var inner: LegacyTransitTimeRollupStore,
    var cache: TtlCache<LegacyTransitTimeRollup>,
    var hits: Int,
    var misses: Int,
) : LegacyTransitTimeRollupStore {
    override fun find(code: String): LegacyTransitTimeRollup? {
        val hit = (cache.get(code) as LegacyTransitTimeRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTransitTimeRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTransitTimeRollup> = inner.list()
}

fun newCachedLegacyTransitTimeRollupStore(inner: LegacyTransitTimeRollupStore, ttl: Int, clock: Clock): LegacyTransitTimeRollupStore {
    return CachedLegacyTransitTimeRollupStore(
        inner = inner,
        cache = newTtlCache<LegacyTransitTimeRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy transit time rollups. Empty fields match all. */
data class LegacyTransitTimeRollupQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTransitTimeRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTransitTimeRollup>): MutableList<LegacyTransitTimeRollup> {
        val matched = filter(items) { candidate: LegacyTransitTimeRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTransitTimeRollups(store: LegacyTransitTimeRollupStore, query: LegacyTransitTimeRollupQuery): MutableList<LegacyTransitTimeRollup> {
    return query.apply(store.list())
}
