package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient duty period proposals by code. */
interface LenientDutyPeriodProposalStore {
    fun find(code: String): LenientDutyPeriodProposal?
    fun save(item: LenientDutyPeriodProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDutyPeriodProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDutyPeriodProposalStore(
    var items: MutableMap<String, LenientDutyPeriodProposal>,
    var order: MutableList<String>,
) : LenientDutyPeriodProposalStore {
    override fun find(code: String): LenientDutyPeriodProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDutyPeriodProposal) {
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

    override fun list(): MutableList<LenientDutyPeriodProposal> {
        val out = mutableListOf<LenientDutyPeriodProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDutyPeriodProposalStore(): LenientDutyPeriodProposalStore {
    return MemoryLenientDutyPeriodProposalStore(
        mutableMapOf<String, LenientDutyPeriodProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDutyPeriodProposalStore(
    var inner: LenientDutyPeriodProposalStore,
    var cache: TtlCache<LenientDutyPeriodProposal>,
    var hits: Int,
    var misses: Int,
) : LenientDutyPeriodProposalStore {
    override fun find(code: String): LenientDutyPeriodProposal? {
        val hit = (cache.get(code) as LenientDutyPeriodProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDutyPeriodProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDutyPeriodProposal> = inner.list()
}

fun newCachedLenientDutyPeriodProposalStore(inner: LenientDutyPeriodProposalStore, ttl: Int, clock: Clock): LenientDutyPeriodProposalStore {
    return CachedLenientDutyPeriodProposalStore(
        inner = inner,
        cache = newTtlCache<LenientDutyPeriodProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient duty period proposals. Empty fields match all. */
data class LenientDutyPeriodProposalQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDutyPeriodProposal): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDutyPeriodProposal>): MutableList<LenientDutyPeriodProposal> {
        val matched = filter(items) { candidate: LenientDutyPeriodProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDutyPeriodProposals(store: LenientDutyPeriodProposalStore, query: LenientDutyPeriodProposalQuery): MutableList<LenientDutyPeriodProposal> {
    return query.apply(store.list())
}
