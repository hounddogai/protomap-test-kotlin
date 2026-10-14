package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge proposals by code. */
interface EvChargeProposalStore {
    fun find(code: String): EvChargeProposal?
    fun save(item: EvChargeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeProposalStore(
    var items: MutableMap<String, EvChargeProposal>,
    var order: MutableList<String>,
) : EvChargeProposalStore {
    override fun find(code: String): EvChargeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeProposal) {
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

    override fun list(): MutableList<EvChargeProposal> {
        val out = mutableListOf<EvChargeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeProposalStore(): EvChargeProposalStore {
    return MemoryEvChargeProposalStore(mutableMapOf<String, EvChargeProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeProposalStore(
    var inner: EvChargeProposalStore,
    var cache: TtlCache<EvChargeProposal>,
    var hits: Int,
    var misses: Int,
) : EvChargeProposalStore {
    override fun find(code: String): EvChargeProposal? {
        val hit = (cache.get(code) as EvChargeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeProposal> = inner.list()
}

fun newCachedEvChargeProposalStore(inner: EvChargeProposalStore, ttl: Int, clock: Clock): EvChargeProposalStore {
    return CachedEvChargeProposalStore(
        inner = inner,
        cache = newTtlCache<EvChargeProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge proposals. Empty fields match all. */
data class EvChargeProposalQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeProposal): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeProposal>): MutableList<EvChargeProposal> {
        val matched = filter(items) { candidate: EvChargeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeProposals(store: EvChargeProposalStore, query: EvChargeProposalQuery): MutableList<EvChargeProposal> {
    return query.apply(store.list())
}
