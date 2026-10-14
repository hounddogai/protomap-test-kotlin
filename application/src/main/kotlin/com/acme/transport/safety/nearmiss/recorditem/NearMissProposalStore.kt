package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss proposals by code. */
interface NearMissProposalStore {
    fun find(code: String): NearMissProposal?
    fun save(item: NearMissProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissProposalStore(
    var items: MutableMap<String, NearMissProposal>,
    var order: MutableList<String>,
) : NearMissProposalStore {
    override fun find(code: String): NearMissProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissProposal) {
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

    override fun list(): MutableList<NearMissProposal> {
        val out = mutableListOf<NearMissProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissProposalStore(): NearMissProposalStore {
    return MemoryNearMissProposalStore(mutableMapOf<String, NearMissProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissProposalStore(
    var inner: NearMissProposalStore,
    var cache: TtlCache<NearMissProposal>,
    var hits: Int,
    var misses: Int,
) : NearMissProposalStore {
    override fun find(code: String): NearMissProposal? {
        val hit = (cache.get(code) as NearMissProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissProposal> = inner.list()
}

fun newCachedNearMissProposalStore(inner: NearMissProposalStore, ttl: Int, clock: Clock): NearMissProposalStore {
    return CachedNearMissProposalStore(
        inner = inner,
        cache = newTtlCache<NearMissProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss proposals. Empty fields match all. */
data class NearMissProposalQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissProposal): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissProposal>): MutableList<NearMissProposal> {
        val matched = filter(items) { candidate: NearMissProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissProposals(store: NearMissProposalStore, query: NearMissProposalQuery): MutableList<NearMissProposal> {
    return query.apply(store.list())
}
