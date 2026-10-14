package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break proposals by code. */
interface RestBreakProposalStore {
    fun find(code: String): RestBreakProposal?
    fun save(item: RestBreakProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakProposalStore(
    var items: MutableMap<String, RestBreakProposal>,
    var order: MutableList<String>,
) : RestBreakProposalStore {
    override fun find(code: String): RestBreakProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakProposal) {
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

    override fun list(): MutableList<RestBreakProposal> {
        val out = mutableListOf<RestBreakProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakProposalStore(): RestBreakProposalStore {
    return MemoryRestBreakProposalStore(mutableMapOf<String, RestBreakProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakProposalStore(
    var inner: RestBreakProposalStore,
    var cache: TtlCache<RestBreakProposal>,
    var hits: Int,
    var misses: Int,
) : RestBreakProposalStore {
    override fun find(code: String): RestBreakProposal? {
        val hit = (cache.get(code) as RestBreakProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakProposal> = inner.list()
}

fun newCachedRestBreakProposalStore(inner: RestBreakProposalStore, ttl: Int, clock: Clock): RestBreakProposalStore {
    return CachedRestBreakProposalStore(
        inner = inner,
        cache = newTtlCache<RestBreakProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break proposals. Empty fields match all. */
data class RestBreakProposalQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakProposal>): MutableList<RestBreakProposal> {
        val matched = filter(items) { candidate: RestBreakProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakProposals(store: RestBreakProposalStore, query: RestBreakProposalQuery): MutableList<RestBreakProposal> {
    return query.apply(store.list())
}
