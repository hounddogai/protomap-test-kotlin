package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rate card assignments by code. */
interface LegacyRateCardAssignmentStore {
    fun find(code: String): LegacyRateCardAssignment?
    fun save(item: LegacyRateCardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRateCardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRateCardAssignmentStore(
    var items: MutableMap<String, LegacyRateCardAssignment>,
    var order: MutableList<String>,
) : LegacyRateCardAssignmentStore {
    override fun find(code: String): LegacyRateCardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRateCardAssignment) {
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

    override fun list(): MutableList<LegacyRateCardAssignment> {
        val out = mutableListOf<LegacyRateCardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRateCardAssignmentStore(): LegacyRateCardAssignmentStore {
    return MemoryLegacyRateCardAssignmentStore(
        mutableMapOf<String, LegacyRateCardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRateCardAssignmentStore(
    var inner: LegacyRateCardAssignmentStore,
    var cache: TtlCache<LegacyRateCardAssignment>,
    var hits: Int,
    var misses: Int,
) : LegacyRateCardAssignmentStore {
    override fun find(code: String): LegacyRateCardAssignment? {
        val hit = (cache.get(code) as LegacyRateCardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRateCardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRateCardAssignment> = inner.list()
}

fun newCachedLegacyRateCardAssignmentStore(inner: LegacyRateCardAssignmentStore, ttl: Int, clock: Clock): LegacyRateCardAssignmentStore {
    return CachedLegacyRateCardAssignmentStore(
        inner = inner,
        cache = newTtlCache<LegacyRateCardAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rate card assignments. Empty fields match all. */
data class LegacyRateCardAssignmentQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRateCardAssignment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRateCardAssignment>): MutableList<LegacyRateCardAssignment> {
        val matched = filter(items) { candidate: LegacyRateCardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRateCardAssignments(store: LegacyRateCardAssignmentStore, query: LegacyRateCardAssignmentQuery): MutableList<LegacyRateCardAssignment> {
    return query.apply(store.list())
}
