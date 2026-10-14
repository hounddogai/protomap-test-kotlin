package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual dock door proposals by code. */
interface ManualDockDoorProposalStore {
    fun find(code: String): ManualDockDoorProposal?
    fun save(item: ManualDockDoorProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDockDoorProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDockDoorProposalStore(
    var items: MutableMap<String, ManualDockDoorProposal>,
    var order: MutableList<String>,
) : ManualDockDoorProposalStore {
    override fun find(code: String): ManualDockDoorProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDockDoorProposal) {
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

    override fun list(): MutableList<ManualDockDoorProposal> {
        val out = mutableListOf<ManualDockDoorProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDockDoorProposalStore(): ManualDockDoorProposalStore {
    return MemoryManualDockDoorProposalStore(
        mutableMapOf<String, ManualDockDoorProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDockDoorProposalStore(
    var inner: ManualDockDoorProposalStore,
    var cache: TtlCache<ManualDockDoorProposal>,
    var hits: Int,
    var misses: Int,
) : ManualDockDoorProposalStore {
    override fun find(code: String): ManualDockDoorProposal? {
        val hit = (cache.get(code) as ManualDockDoorProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDockDoorProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDockDoorProposal> = inner.list()
}

fun newCachedManualDockDoorProposalStore(inner: ManualDockDoorProposalStore, ttl: Int, clock: Clock): ManualDockDoorProposalStore {
    return CachedManualDockDoorProposalStore(
        inner = inner,
        cache = newTtlCache<ManualDockDoorProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dock door proposals. Empty fields match all. */
data class ManualDockDoorProposalQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDockDoorProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDockDoorProposal>): MutableList<ManualDockDoorProposal> {
        val matched = filter(items) { candidate: ManualDockDoorProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDockDoorProposals(store: ManualDockDoorProposalStore, query: ManualDockDoorProposalQuery): MutableList<ManualDockDoorProposal> {
    return query.apply(store.list())
}
