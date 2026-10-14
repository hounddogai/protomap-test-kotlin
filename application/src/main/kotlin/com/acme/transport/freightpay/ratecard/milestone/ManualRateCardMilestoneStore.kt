package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual rate card milestones by code. */
interface ManualRateCardMilestoneStore {
    fun find(code: String): ManualRateCardMilestone?
    fun save(item: ManualRateCardMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRateCardMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRateCardMilestoneStore(
    var items: MutableMap<String, ManualRateCardMilestone>,
    var order: MutableList<String>,
) : ManualRateCardMilestoneStore {
    override fun find(code: String): ManualRateCardMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRateCardMilestone) {
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

    override fun list(): MutableList<ManualRateCardMilestone> {
        val out = mutableListOf<ManualRateCardMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRateCardMilestoneStore(): ManualRateCardMilestoneStore {
    return MemoryManualRateCardMilestoneStore(
        mutableMapOf<String, ManualRateCardMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRateCardMilestoneStore(
    var inner: ManualRateCardMilestoneStore,
    var cache: TtlCache<ManualRateCardMilestone>,
    var hits: Int,
    var misses: Int,
) : ManualRateCardMilestoneStore {
    override fun find(code: String): ManualRateCardMilestone? {
        val hit = (cache.get(code) as ManualRateCardMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRateCardMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRateCardMilestone> = inner.list()
}

fun newCachedManualRateCardMilestoneStore(inner: ManualRateCardMilestoneStore, ttl: Int, clock: Clock): ManualRateCardMilestoneStore {
    return CachedManualRateCardMilestoneStore(
        inner = inner,
        cache = newTtlCache<ManualRateCardMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual rate card milestones. Empty fields match all. */
data class ManualRateCardMilestoneQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRateCardMilestone): Boolean {
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

    fun apply(items: MutableList<ManualRateCardMilestone>): MutableList<ManualRateCardMilestone> {
        val matched = filter(items) { candidate: ManualRateCardMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRateCardMilestones(store: ManualRateCardMilestoneStore, query: ManualRateCardMilestoneQuery): MutableList<ManualRateCardMilestone> {
    return query.apply(store.list())
}
