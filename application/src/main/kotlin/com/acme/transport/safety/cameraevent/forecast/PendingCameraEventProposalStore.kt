package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending camera event proposals by code. */
interface PendingCameraEventProposalStore {
    fun find(code: String): PendingCameraEventProposal?
    fun save(item: PendingCameraEventProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCameraEventProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCameraEventProposalStore(
    var items: MutableMap<String, PendingCameraEventProposal>,
    var order: MutableList<String>,
) : PendingCameraEventProposalStore {
    override fun find(code: String): PendingCameraEventProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCameraEventProposal) {
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

    override fun list(): MutableList<PendingCameraEventProposal> {
        val out = mutableListOf<PendingCameraEventProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCameraEventProposalStore(): PendingCameraEventProposalStore {
    return MemoryPendingCameraEventProposalStore(
        mutableMapOf<String, PendingCameraEventProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCameraEventProposalStore(
    var inner: PendingCameraEventProposalStore,
    var cache: TtlCache<PendingCameraEventProposal>,
    var hits: Int,
    var misses: Int,
) : PendingCameraEventProposalStore {
    override fun find(code: String): PendingCameraEventProposal? {
        val hit = (cache.get(code) as PendingCameraEventProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCameraEventProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCameraEventProposal> = inner.list()
}

fun newCachedPendingCameraEventProposalStore(inner: PendingCameraEventProposalStore, ttl: Int, clock: Clock): PendingCameraEventProposalStore {
    return CachedPendingCameraEventProposalStore(
        inner = inner,
        cache = newTtlCache<PendingCameraEventProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending camera event proposals. Empty fields match all. */
data class PendingCameraEventProposalQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCameraEventProposal): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCameraEventProposal>): MutableList<PendingCameraEventProposal> {
        val matched = filter(items) { candidate: PendingCameraEventProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCameraEventProposals(store: PendingCameraEventProposalStore, query: PendingCameraEventProposalQuery): MutableList<PendingCameraEventProposal> {
    return query.apply(store.list())
}
