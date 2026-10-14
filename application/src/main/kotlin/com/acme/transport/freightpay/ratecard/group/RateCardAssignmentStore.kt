package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card assignments by code. */
interface RateCardAssignmentStore {
    fun find(code: String): RateCardAssignment?
    fun save(item: RateCardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardAssignmentStore(
    var items: MutableMap<String, RateCardAssignment>,
    var order: MutableList<String>,
) : RateCardAssignmentStore {
    override fun find(code: String): RateCardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardAssignment) {
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

    override fun list(): MutableList<RateCardAssignment> {
        val out = mutableListOf<RateCardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardAssignmentStore(): RateCardAssignmentStore {
    return MemoryRateCardAssignmentStore(mutableMapOf<String, RateCardAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardAssignmentStore(
    var inner: RateCardAssignmentStore,
    var cache: TtlCache<RateCardAssignment>,
    var hits: Int,
    var misses: Int,
) : RateCardAssignmentStore {
    override fun find(code: String): RateCardAssignment? {
        val hit = (cache.get(code) as RateCardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardAssignment> = inner.list()
}

fun newCachedRateCardAssignmentStore(inner: RateCardAssignmentStore, ttl: Int, clock: Clock): RateCardAssignmentStore {
    return CachedRateCardAssignmentStore(
        inner = inner,
        cache = newTtlCache<RateCardAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card assignments. Empty fields match all. */
data class RateCardAssignmentQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardAssignment>): MutableList<RateCardAssignment> {
        val matched = filter(items) { candidate: RateCardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardAssignments(store: RateCardAssignmentStore, query: RateCardAssignmentQuery): MutableList<RateCardAssignment> {
    return query.apply(store.list())
}
