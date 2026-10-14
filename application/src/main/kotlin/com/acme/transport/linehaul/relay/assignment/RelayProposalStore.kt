package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay proposals by code. */
interface RelayProposalStore {
    fun find(code: String): RelayProposal?
    fun save(item: RelayProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayProposalStore(
    var items: MutableMap<String, RelayProposal>,
    var order: MutableList<String>,
) : RelayProposalStore {
    override fun find(code: String): RelayProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayProposal) {
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

    override fun list(): MutableList<RelayProposal> {
        val out = mutableListOf<RelayProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayProposalStore(): RelayProposalStore {
    return MemoryRelayProposalStore(mutableMapOf<String, RelayProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayProposalStore(
    var inner: RelayProposalStore,
    var cache: TtlCache<RelayProposal>,
    var hits: Int,
    var misses: Int,
) : RelayProposalStore {
    override fun find(code: String): RelayProposal? {
        val hit = (cache.get(code) as RelayProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayProposal> = inner.list()
}

fun newCachedRelayProposalStore(inner: RelayProposalStore, ttl: Int, clock: Clock): RelayProposalStore {
    return CachedRelayProposalStore(
        inner = inner,
        cache = newTtlCache<RelayProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay proposals. Empty fields match all. */
data class RelayProposalQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayProposal): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayProposal>): MutableList<RelayProposal> {
        val matched = filter(items) { candidate: RelayProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayProposals(store: RelayProposalStore, query: RelayProposalQuery): MutableList<RelayProposal> {
    return query.apply(store.list())
}
