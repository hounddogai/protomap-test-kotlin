package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door proposals by code. */
interface DockDoorProposalStore {
    fun find(code: String): DockDoorProposal?
    fun save(item: DockDoorProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorProposalStore(
    var items: MutableMap<String, DockDoorProposal>,
    var order: MutableList<String>,
) : DockDoorProposalStore {
    override fun find(code: String): DockDoorProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorProposal) {
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

    override fun list(): MutableList<DockDoorProposal> {
        val out = mutableListOf<DockDoorProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorProposalStore(): DockDoorProposalStore {
    return MemoryDockDoorProposalStore(mutableMapOf<String, DockDoorProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorProposalStore(
    var inner: DockDoorProposalStore,
    var cache: TtlCache<DockDoorProposal>,
    var hits: Int,
    var misses: Int,
) : DockDoorProposalStore {
    override fun find(code: String): DockDoorProposal? {
        val hit = (cache.get(code) as DockDoorProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorProposal> = inner.list()
}

fun newCachedDockDoorProposalStore(inner: DockDoorProposalStore, ttl: Int, clock: Clock): DockDoorProposalStore {
    return CachedDockDoorProposalStore(
        inner = inner,
        cache = newTtlCache<DockDoorProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door proposals. Empty fields match all. */
data class DockDoorProposalQuery(
    var lane: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorProposal): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorProposal>): MutableList<DockDoorProposal> {
        val matched = filter(items) { candidate: DockDoorProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorProposals(store: DockDoorProposalStore, query: DockDoorProposalQuery): MutableList<DockDoorProposal> {
    return query.apply(store.list())
}
