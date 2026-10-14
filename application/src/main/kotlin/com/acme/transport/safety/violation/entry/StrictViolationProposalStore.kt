package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict violation proposals by code. */
interface StrictViolationProposalStore {
    fun find(code: String): StrictViolationProposal?
    fun save(item: StrictViolationProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictViolationProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictViolationProposalStore(
    var items: MutableMap<String, StrictViolationProposal>,
    var order: MutableList<String>,
) : StrictViolationProposalStore {
    override fun find(code: String): StrictViolationProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictViolationProposal) {
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

    override fun list(): MutableList<StrictViolationProposal> {
        val out = mutableListOf<StrictViolationProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictViolationProposalStore(): StrictViolationProposalStore {
    return MemoryStrictViolationProposalStore(
        mutableMapOf<String, StrictViolationProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictViolationProposalStore(
    var inner: StrictViolationProposalStore,
    var cache: TtlCache<StrictViolationProposal>,
    var hits: Int,
    var misses: Int,
) : StrictViolationProposalStore {
    override fun find(code: String): StrictViolationProposal? {
        val hit = (cache.get(code) as StrictViolationProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictViolationProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictViolationProposal> = inner.list()
}

fun newCachedStrictViolationProposalStore(inner: StrictViolationProposalStore, ttl: Int, clock: Clock): StrictViolationProposalStore {
    return CachedStrictViolationProposalStore(
        inner = inner,
        cache = newTtlCache<StrictViolationProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict violation proposals. Empty fields match all. */
data class StrictViolationProposalQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictViolationProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<StrictViolationProposal>): MutableList<StrictViolationProposal> {
        val matched = filter(items) { candidate: StrictViolationProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictViolationProposals(store: StrictViolationProposalStore, query: StrictViolationProposalQuery): MutableList<StrictViolationProposal> {
    return query.apply(store.list())
}
