package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rate card assignments by code. */
interface GlobalRateCardAssignmentStore {
    fun find(code: String): GlobalRateCardAssignment?
    fun save(item: GlobalRateCardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRateCardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRateCardAssignmentStore(
    var items: MutableMap<String, GlobalRateCardAssignment>,
    var order: MutableList<String>,
) : GlobalRateCardAssignmentStore {
    override fun find(code: String): GlobalRateCardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRateCardAssignment) {
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

    override fun list(): MutableList<GlobalRateCardAssignment> {
        val out = mutableListOf<GlobalRateCardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRateCardAssignmentStore(): GlobalRateCardAssignmentStore {
    return MemoryGlobalRateCardAssignmentStore(
        mutableMapOf<String, GlobalRateCardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRateCardAssignmentStore(
    var inner: GlobalRateCardAssignmentStore,
    var cache: TtlCache<GlobalRateCardAssignment>,
    var hits: Int,
    var misses: Int,
) : GlobalRateCardAssignmentStore {
    override fun find(code: String): GlobalRateCardAssignment? {
        val hit = (cache.get(code) as GlobalRateCardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRateCardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRateCardAssignment> = inner.list()
}

fun newCachedGlobalRateCardAssignmentStore(inner: GlobalRateCardAssignmentStore, ttl: Int, clock: Clock): GlobalRateCardAssignmentStore {
    return CachedGlobalRateCardAssignmentStore(
        inner = inner,
        cache = newTtlCache<GlobalRateCardAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rate card assignments. Empty fields match all. */
data class GlobalRateCardAssignmentQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRateCardAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRateCardAssignment>): MutableList<GlobalRateCardAssignment> {
        val matched = filter(items) { candidate: GlobalRateCardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRateCardAssignments(store: GlobalRateCardAssignmentStore, query: GlobalRateCardAssignmentQuery): MutableList<GlobalRateCardAssignment> {
    return query.apply(store.list())
}
