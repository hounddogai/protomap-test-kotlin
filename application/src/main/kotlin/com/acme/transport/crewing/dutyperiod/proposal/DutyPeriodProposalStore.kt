package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period proposals by code. */
interface DutyPeriodProposalStore {
    fun find(code: String): DutyPeriodProposal?
    fun save(item: DutyPeriodProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodProposalStore(
    var items: MutableMap<String, DutyPeriodProposal>,
    var order: MutableList<String>,
) : DutyPeriodProposalStore {
    override fun find(code: String): DutyPeriodProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodProposal) {
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

    override fun list(): MutableList<DutyPeriodProposal> {
        val out = mutableListOf<DutyPeriodProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodProposalStore(): DutyPeriodProposalStore {
    return MemoryDutyPeriodProposalStore(mutableMapOf<String, DutyPeriodProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodProposalStore(
    var inner: DutyPeriodProposalStore,
    var cache: TtlCache<DutyPeriodProposal>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodProposalStore {
    override fun find(code: String): DutyPeriodProposal? {
        val hit = (cache.get(code) as DutyPeriodProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodProposal> = inner.list()
}

fun newCachedDutyPeriodProposalStore(inner: DutyPeriodProposalStore, ttl: Int, clock: Clock): DutyPeriodProposalStore {
    return CachedDutyPeriodProposalStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period proposals. Empty fields match all. */
data class DutyPeriodProposalQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodProposal): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<DutyPeriodProposal>): MutableList<DutyPeriodProposal> {
        val matched = filter(items) { candidate: DutyPeriodProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodProposals(store: DutyPeriodProposalStore, query: DutyPeriodProposalQuery): MutableList<DutyPeriodProposal> {
    return query.apply(store.list())
}
