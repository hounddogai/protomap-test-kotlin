package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker milestones by code. */
interface BrokerMilestoneStore {
    fun find(code: String): BrokerMilestone?
    fun save(item: BrokerMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerMilestoneStore(
    var items: MutableMap<String, BrokerMilestone>,
    var order: MutableList<String>,
) : BrokerMilestoneStore {
    override fun find(code: String): BrokerMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerMilestone) {
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

    override fun list(): MutableList<BrokerMilestone> {
        val out = mutableListOf<BrokerMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerMilestoneStore(): BrokerMilestoneStore {
    return MemoryBrokerMilestoneStore(mutableMapOf<String, BrokerMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerMilestoneStore(
    var inner: BrokerMilestoneStore,
    var cache: TtlCache<BrokerMilestone>,
    var hits: Int,
    var misses: Int,
) : BrokerMilestoneStore {
    override fun find(code: String): BrokerMilestone? {
        val hit = (cache.get(code) as BrokerMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerMilestone> = inner.list()
}

fun newCachedBrokerMilestoneStore(inner: BrokerMilestoneStore, ttl: Int, clock: Clock): BrokerMilestoneStore {
    return CachedBrokerMilestoneStore(
        inner = inner,
        cache = newTtlCache<BrokerMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker milestones. Empty fields match all. */
data class BrokerMilestoneQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerMilestone): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerMilestone>): MutableList<BrokerMilestone> {
        val matched = filter(items) { candidate: BrokerMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerMilestones(store: BrokerMilestoneStore, query: BrokerMilestoneQuery): MutableList<BrokerMilestone> {
    return query.apply(store.list())
}
