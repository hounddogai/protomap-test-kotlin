package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional dock door proposals by code. */
interface RegionalDockDoorProposalStore {
    fun find(code: String): RegionalDockDoorProposal?
    fun save(item: RegionalDockDoorProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDockDoorProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDockDoorProposalStore(
    var items: MutableMap<String, RegionalDockDoorProposal>,
    var order: MutableList<String>,
) : RegionalDockDoorProposalStore {
    override fun find(code: String): RegionalDockDoorProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDockDoorProposal) {
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

    override fun list(): MutableList<RegionalDockDoorProposal> {
        val out = mutableListOf<RegionalDockDoorProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDockDoorProposalStore(): RegionalDockDoorProposalStore {
    return MemoryRegionalDockDoorProposalStore(
        mutableMapOf<String, RegionalDockDoorProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDockDoorProposalStore(
    var inner: RegionalDockDoorProposalStore,
    var cache: TtlCache<RegionalDockDoorProposal>,
    var hits: Int,
    var misses: Int,
) : RegionalDockDoorProposalStore {
    override fun find(code: String): RegionalDockDoorProposal? {
        val hit = (cache.get(code) as RegionalDockDoorProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDockDoorProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDockDoorProposal> = inner.list()
}

fun newCachedRegionalDockDoorProposalStore(inner: RegionalDockDoorProposalStore, ttl: Int, clock: Clock): RegionalDockDoorProposalStore {
    return CachedRegionalDockDoorProposalStore(
        inner = inner,
        cache = newTtlCache<RegionalDockDoorProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional dock door proposals. Empty fields match all. */
data class RegionalDockDoorProposalQuery(
    var channel: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDockDoorProposal): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDockDoorProposal>): MutableList<RegionalDockDoorProposal> {
        val matched = filter(items) { candidate: RegionalDockDoorProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDockDoorProposals(store: RegionalDockDoorProposalStore, query: RegionalDockDoorProposalQuery): MutableList<RegionalDockDoorProposal> {
    return query.apply(store.list())
}
