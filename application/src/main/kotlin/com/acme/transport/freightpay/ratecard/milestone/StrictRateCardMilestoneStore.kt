package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rate card milestones by code. */
interface StrictRateCardMilestoneStore {
    fun find(code: String): StrictRateCardMilestone?
    fun save(item: StrictRateCardMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRateCardMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRateCardMilestoneStore(
    var items: MutableMap<String, StrictRateCardMilestone>,
    var order: MutableList<String>,
) : StrictRateCardMilestoneStore {
    override fun find(code: String): StrictRateCardMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRateCardMilestone) {
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

    override fun list(): MutableList<StrictRateCardMilestone> {
        val out = mutableListOf<StrictRateCardMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRateCardMilestoneStore(): StrictRateCardMilestoneStore {
    return MemoryStrictRateCardMilestoneStore(
        mutableMapOf<String, StrictRateCardMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRateCardMilestoneStore(
    var inner: StrictRateCardMilestoneStore,
    var cache: TtlCache<StrictRateCardMilestone>,
    var hits: Int,
    var misses: Int,
) : StrictRateCardMilestoneStore {
    override fun find(code: String): StrictRateCardMilestone? {
        val hit = (cache.get(code) as StrictRateCardMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRateCardMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRateCardMilestone> = inner.list()
}

fun newCachedStrictRateCardMilestoneStore(inner: StrictRateCardMilestoneStore, ttl: Int, clock: Clock): StrictRateCardMilestoneStore {
    return CachedStrictRateCardMilestoneStore(
        inner = inner,
        cache = newTtlCache<StrictRateCardMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rate card milestones. Empty fields match all. */
data class StrictRateCardMilestoneQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRateCardMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRateCardMilestone>): MutableList<StrictRateCardMilestone> {
        val matched = filter(items) { candidate: StrictRateCardMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRateCardMilestones(store: StrictRateCardMilestoneStore, query: StrictRateCardMilestoneQuery): MutableList<StrictRateCardMilestone> {
    return query.apply(store.list())
}
