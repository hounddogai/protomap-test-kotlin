package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled capacity proposals by code. */
interface ScheduledCapacityProposalStore {
    fun find(code: String): ScheduledCapacityProposal?
    fun save(item: ScheduledCapacityProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCapacityProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCapacityProposalStore(
    var items: MutableMap<String, ScheduledCapacityProposal>,
    var order: MutableList<String>,
) : ScheduledCapacityProposalStore {
    override fun find(code: String): ScheduledCapacityProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCapacityProposal) {
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

    override fun list(): MutableList<ScheduledCapacityProposal> {
        val out = mutableListOf<ScheduledCapacityProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCapacityProposalStore(): ScheduledCapacityProposalStore {
    return MemoryScheduledCapacityProposalStore(
        mutableMapOf<String, ScheduledCapacityProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCapacityProposalStore(
    var inner: ScheduledCapacityProposalStore,
    var cache: TtlCache<ScheduledCapacityProposal>,
    var hits: Int,
    var misses: Int,
) : ScheduledCapacityProposalStore {
    override fun find(code: String): ScheduledCapacityProposal? {
        val hit = (cache.get(code) as ScheduledCapacityProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCapacityProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCapacityProposal> = inner.list()
}

fun newCachedScheduledCapacityProposalStore(inner: ScheduledCapacityProposalStore, ttl: Int, clock: Clock): ScheduledCapacityProposalStore {
    return CachedScheduledCapacityProposalStore(
        inner = inner,
        cache = newTtlCache<ScheduledCapacityProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled capacity proposals. Empty fields match all. */
data class ScheduledCapacityProposalQuery(
    var source: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCapacityProposal): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCapacityProposal>): MutableList<ScheduledCapacityProposal> {
        val matched = filter(items) { candidate: ScheduledCapacityProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCapacityProposals(store: ScheduledCapacityProposalStore, query: ScheduledCapacityProposalQuery): MutableList<ScheduledCapacityProposal> {
    return query.apply(store.list())
}
