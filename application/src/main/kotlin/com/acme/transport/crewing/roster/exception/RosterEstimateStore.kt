package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster estimates by code. */
interface RosterEstimateStore {
    fun find(code: String): RosterEstimate?
    fun save(item: RosterEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterEstimateStore(
    var items: MutableMap<String, RosterEstimate>,
    var order: MutableList<String>,
) : RosterEstimateStore {
    override fun find(code: String): RosterEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterEstimate) {
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

    override fun list(): MutableList<RosterEstimate> {
        val out = mutableListOf<RosterEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterEstimateStore(): RosterEstimateStore {
    return MemoryRosterEstimateStore(mutableMapOf<String, RosterEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterEstimateStore(
    var inner: RosterEstimateStore,
    var cache: TtlCache<RosterEstimate>,
    var hits: Int,
    var misses: Int,
) : RosterEstimateStore {
    override fun find(code: String): RosterEstimate? {
        val hit = (cache.get(code) as RosterEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterEstimate> = inner.list()
}

fun newCachedRosterEstimateStore(inner: RosterEstimateStore, ttl: Int, clock: Clock): RosterEstimateStore {
    return CachedRosterEstimateStore(
        inner = inner,
        cache = newTtlCache<RosterEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster estimates. Empty fields match all. */
data class RosterEstimateQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterEstimate>): MutableList<RosterEstimate> {
        val matched = filter(items) { candidate: RosterEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterEstimates(store: RosterEstimateStore, query: RosterEstimateQuery): MutableList<RosterEstimate> {
    return query.apply(store.list())
}
