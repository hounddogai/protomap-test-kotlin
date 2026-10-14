package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending proof of delivery milestones by code. */
interface PendingProofOfDeliveryMilestoneStore {
    fun find(code: String): PendingProofOfDeliveryMilestone?
    fun save(item: PendingProofOfDeliveryMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingProofOfDeliveryMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingProofOfDeliveryMilestoneStore(
    var items: MutableMap<String, PendingProofOfDeliveryMilestone>,
    var order: MutableList<String>,
) : PendingProofOfDeliveryMilestoneStore {
    override fun find(code: String): PendingProofOfDeliveryMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingProofOfDeliveryMilestone) {
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

    override fun list(): MutableList<PendingProofOfDeliveryMilestone> {
        val out = mutableListOf<PendingProofOfDeliveryMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingProofOfDeliveryMilestoneStore(): PendingProofOfDeliveryMilestoneStore {
    return MemoryPendingProofOfDeliveryMilestoneStore(
        mutableMapOf<String, PendingProofOfDeliveryMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingProofOfDeliveryMilestoneStore(
    var inner: PendingProofOfDeliveryMilestoneStore,
    var cache: TtlCache<PendingProofOfDeliveryMilestone>,
    var hits: Int,
    var misses: Int,
) : PendingProofOfDeliveryMilestoneStore {
    override fun find(code: String): PendingProofOfDeliveryMilestone? {
        val hit = (cache.get(code) as PendingProofOfDeliveryMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingProofOfDeliveryMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingProofOfDeliveryMilestone> = inner.list()
}

fun newCachedPendingProofOfDeliveryMilestoneStore(inner: PendingProofOfDeliveryMilestoneStore, ttl: Int, clock: Clock): PendingProofOfDeliveryMilestoneStore {
    return CachedPendingProofOfDeliveryMilestoneStore(
        inner = inner,
        cache = newTtlCache<PendingProofOfDeliveryMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending proof of delivery milestones. Empty fields match all. */
data class PendingProofOfDeliveryMilestoneQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingProofOfDeliveryMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingProofOfDeliveryMilestone>): MutableList<PendingProofOfDeliveryMilestone> {
        val matched = filter(items) { candidate: PendingProofOfDeliveryMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingProofOfDeliveryMilestones(store: PendingProofOfDeliveryMilestoneStore, query: PendingProofOfDeliveryMilestoneQuery): MutableList<PendingProofOfDeliveryMilestone> {
    return query.apply(store.list())
}
