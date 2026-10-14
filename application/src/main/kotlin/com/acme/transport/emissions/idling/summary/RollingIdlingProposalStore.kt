package com.acme.transport.emissions.idling.summary

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling idling proposals by code. */
interface RollingIdlingProposalStore {
    fun find(code: String): RollingIdlingProposal?
    fun save(item: RollingIdlingProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingIdlingProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingIdlingProposalStore(
    var items: MutableMap<String, RollingIdlingProposal>,
    var order: MutableList<String>,
) : RollingIdlingProposalStore {
    override fun find(code: String): RollingIdlingProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingIdlingProposal) {
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

    override fun list(): MutableList<RollingIdlingProposal> {
        val out = mutableListOf<RollingIdlingProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingIdlingProposalStore(): RollingIdlingProposalStore {
    return MemoryRollingIdlingProposalStore(
        mutableMapOf<String, RollingIdlingProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingIdlingProposalStore(
    var inner: RollingIdlingProposalStore,
    var cache: TtlCache<RollingIdlingProposal>,
    var hits: Int,
    var misses: Int,
) : RollingIdlingProposalStore {
    override fun find(code: String): RollingIdlingProposal? {
        val hit = (cache.get(code) as RollingIdlingProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingIdlingProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingIdlingProposal> = inner.list()
}

fun newCachedRollingIdlingProposalStore(inner: RollingIdlingProposalStore, ttl: Int, clock: Clock): RollingIdlingProposalStore {
    return CachedRollingIdlingProposalStore(
        inner = inner,
        cache = newTtlCache<RollingIdlingProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling idling proposals. Empty fields match all. */
data class RollingIdlingProposalQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingIdlingProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingIdlingProposal>): MutableList<RollingIdlingProposal> {
        val matched = filter(items) { candidate: RollingIdlingProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingIdlingProposals(store: RollingIdlingProposalStore, query: RollingIdlingProposalQuery): MutableList<RollingIdlingProposal> {
    return query.apply(store.list())
}
