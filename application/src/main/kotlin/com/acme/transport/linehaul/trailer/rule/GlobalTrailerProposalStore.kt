package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global trailer proposals by code. */
interface GlobalTrailerProposalStore {
    fun find(code: String): GlobalTrailerProposal?
    fun save(item: GlobalTrailerProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerProposalStore(
    var items: MutableMap<String, GlobalTrailerProposal>,
    var order: MutableList<String>,
) : GlobalTrailerProposalStore {
    override fun find(code: String): GlobalTrailerProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerProposal) {
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

    override fun list(): MutableList<GlobalTrailerProposal> {
        val out = mutableListOf<GlobalTrailerProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerProposalStore(): GlobalTrailerProposalStore {
    return MemoryGlobalTrailerProposalStore(
        mutableMapOf<String, GlobalTrailerProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerProposalStore(
    var inner: GlobalTrailerProposalStore,
    var cache: TtlCache<GlobalTrailerProposal>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerProposalStore {
    override fun find(code: String): GlobalTrailerProposal? {
        val hit = (cache.get(code) as GlobalTrailerProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerProposal> = inner.list()
}

fun newCachedGlobalTrailerProposalStore(inner: GlobalTrailerProposalStore, ttl: Int, clock: Clock): GlobalTrailerProposalStore {
    return CachedGlobalTrailerProposalStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer proposals. Empty fields match all. */
data class GlobalTrailerProposalQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTrailerProposal>): MutableList<GlobalTrailerProposal> {
        val matched = filter(items) { candidate: GlobalTrailerProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerProposals(store: GlobalTrailerProposalStore, query: GlobalTrailerProposalQuery): MutableList<GlobalTrailerProposal> {
    return query.apply(store.list())
}
