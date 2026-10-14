package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling violation proposals by code. */
interface RollingViolationProposalStore {
    fun find(code: String): RollingViolationProposal?
    fun save(item: RollingViolationProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingViolationProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingViolationProposalStore(
    var items: MutableMap<String, RollingViolationProposal>,
    var order: MutableList<String>,
) : RollingViolationProposalStore {
    override fun find(code: String): RollingViolationProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingViolationProposal) {
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

    override fun list(): MutableList<RollingViolationProposal> {
        val out = mutableListOf<RollingViolationProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingViolationProposalStore(): RollingViolationProposalStore {
    return MemoryRollingViolationProposalStore(
        mutableMapOf<String, RollingViolationProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingViolationProposalStore(
    var inner: RollingViolationProposalStore,
    var cache: TtlCache<RollingViolationProposal>,
    var hits: Int,
    var misses: Int,
) : RollingViolationProposalStore {
    override fun find(code: String): RollingViolationProposal? {
        val hit = (cache.get(code) as RollingViolationProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingViolationProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingViolationProposal> = inner.list()
}

fun newCachedRollingViolationProposalStore(inner: RollingViolationProposalStore, ttl: Int, clock: Clock): RollingViolationProposalStore {
    return CachedRollingViolationProposalStore(
        inner = inner,
        cache = newTtlCache<RollingViolationProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling violation proposals. Empty fields match all. */
data class RollingViolationProposalQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingViolationProposal): Boolean {
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

    fun apply(items: MutableList<RollingViolationProposal>): MutableList<RollingViolationProposal> {
        val matched = filter(items) { candidate: RollingViolationProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingViolationProposals(store: RollingViolationProposalStore, query: RollingViolationProposalQuery): MutableList<RollingViolationProposal> {
    return query.apply(store.list())
}
