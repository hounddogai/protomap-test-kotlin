package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching proposals by code. */
interface CoachingProposalStore {
    fun find(code: String): CoachingProposal?
    fun save(item: CoachingProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingProposalStore(
    var items: MutableMap<String, CoachingProposal>,
    var order: MutableList<String>,
) : CoachingProposalStore {
    override fun find(code: String): CoachingProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingProposal) {
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

    override fun list(): MutableList<CoachingProposal> {
        val out = mutableListOf<CoachingProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingProposalStore(): CoachingProposalStore {
    return MemoryCoachingProposalStore(mutableMapOf<String, CoachingProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingProposalStore(
    var inner: CoachingProposalStore,
    var cache: TtlCache<CoachingProposal>,
    var hits: Int,
    var misses: Int,
) : CoachingProposalStore {
    override fun find(code: String): CoachingProposal? {
        val hit = (cache.get(code) as CoachingProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingProposal> = inner.list()
}

fun newCachedCoachingProposalStore(inner: CoachingProposalStore, ttl: Int, clock: Clock): CoachingProposalStore {
    return CachedCoachingProposalStore(
        inner = inner,
        cache = newTtlCache<CoachingProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching proposals. Empty fields match all. */
data class CoachingProposalQuery(
    var unit: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingProposal): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<CoachingProposal>): MutableList<CoachingProposal> {
        val matched = filter(items) { candidate: CoachingProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingProposals(store: CoachingProposalStore, query: CoachingProposalQuery): MutableList<CoachingProposal> {
    return query.apply(store.list())
}
