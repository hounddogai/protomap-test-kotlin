package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rate card assignments by code. */
interface DefaultRateCardAssignmentStore {
    fun find(code: String): DefaultRateCardAssignment?
    fun save(item: DefaultRateCardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRateCardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRateCardAssignmentStore(
    var items: MutableMap<String, DefaultRateCardAssignment>,
    var order: MutableList<String>,
) : DefaultRateCardAssignmentStore {
    override fun find(code: String): DefaultRateCardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRateCardAssignment) {
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

    override fun list(): MutableList<DefaultRateCardAssignment> {
        val out = mutableListOf<DefaultRateCardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRateCardAssignmentStore(): DefaultRateCardAssignmentStore {
    return MemoryDefaultRateCardAssignmentStore(
        mutableMapOf<String, DefaultRateCardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRateCardAssignmentStore(
    var inner: DefaultRateCardAssignmentStore,
    var cache: TtlCache<DefaultRateCardAssignment>,
    var hits: Int,
    var misses: Int,
) : DefaultRateCardAssignmentStore {
    override fun find(code: String): DefaultRateCardAssignment? {
        val hit = (cache.get(code) as DefaultRateCardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRateCardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRateCardAssignment> = inner.list()
}

fun newCachedDefaultRateCardAssignmentStore(inner: DefaultRateCardAssignmentStore, ttl: Int, clock: Clock): DefaultRateCardAssignmentStore {
    return CachedDefaultRateCardAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefaultRateCardAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rate card assignments. Empty fields match all. */
data class DefaultRateCardAssignmentQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRateCardAssignment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRateCardAssignment>): MutableList<DefaultRateCardAssignment> {
        val matched = filter(items) { candidate: DefaultRateCardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRateCardAssignments(store: DefaultRateCardAssignmentStore, query: DefaultRateCardAssignmentQuery): MutableList<DefaultRateCardAssignment> {
    return query.apply(store.list())
}
