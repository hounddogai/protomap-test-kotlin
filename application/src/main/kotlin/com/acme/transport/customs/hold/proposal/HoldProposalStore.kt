package com.acme.transport.customs.hold.proposal

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold proposals by code. */
interface HoldProposalStore {
    fun find(code: String): HoldProposal?
    fun save(item: HoldProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldProposalStore(
    var items: MutableMap<String, HoldProposal>,
    var order: MutableList<String>,
) : HoldProposalStore {
    override fun find(code: String): HoldProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldProposal) {
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

    override fun list(): MutableList<HoldProposal> {
        val out = mutableListOf<HoldProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldProposalStore(): HoldProposalStore {
    return MemoryHoldProposalStore(mutableMapOf<String, HoldProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldProposalStore(
    var inner: HoldProposalStore,
    var cache: TtlCache<HoldProposal>,
    var hits: Int,
    var misses: Int,
) : HoldProposalStore {
    override fun find(code: String): HoldProposal? {
        val hit = (cache.get(code) as HoldProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldProposal> = inner.list()
}

fun newCachedHoldProposalStore(inner: HoldProposalStore, ttl: Int, clock: Clock): HoldProposalStore {
    return CachedHoldProposalStore(
        inner = inner,
        cache = newTtlCache<HoldProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold proposals. Empty fields match all. */
data class HoldProposalQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldProposal>): MutableList<HoldProposal> {
        val matched = filter(items) { candidate: HoldProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldProposals(store: HoldProposalStore, query: HoldProposalQuery): MutableList<HoldProposal> {
    return query.apply(store.list())
}
