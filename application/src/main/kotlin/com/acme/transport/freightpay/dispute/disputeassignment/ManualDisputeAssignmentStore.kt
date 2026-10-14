package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual dispute assignments by code. */
interface ManualDisputeAssignmentStore {
    fun find(code: String): ManualDisputeAssignment?
    fun save(item: ManualDisputeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDisputeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDisputeAssignmentStore(
    var items: MutableMap<String, ManualDisputeAssignment>,
    var order: MutableList<String>,
) : ManualDisputeAssignmentStore {
    override fun find(code: String): ManualDisputeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDisputeAssignment) {
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

    override fun list(): MutableList<ManualDisputeAssignment> {
        val out = mutableListOf<ManualDisputeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDisputeAssignmentStore(): ManualDisputeAssignmentStore {
    return MemoryManualDisputeAssignmentStore(
        mutableMapOf<String, ManualDisputeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDisputeAssignmentStore(
    var inner: ManualDisputeAssignmentStore,
    var cache: TtlCache<ManualDisputeAssignment>,
    var hits: Int,
    var misses: Int,
) : ManualDisputeAssignmentStore {
    override fun find(code: String): ManualDisputeAssignment? {
        val hit = (cache.get(code) as ManualDisputeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDisputeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDisputeAssignment> = inner.list()
}

fun newCachedManualDisputeAssignmentStore(inner: ManualDisputeAssignmentStore, ttl: Int, clock: Clock): ManualDisputeAssignmentStore {
    return CachedManualDisputeAssignmentStore(
        inner = inner,
        cache = newTtlCache<ManualDisputeAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dispute assignments. Empty fields match all. */
data class ManualDisputeAssignmentQuery(
    var channel: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDisputeAssignment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDisputeAssignment>): MutableList<ManualDisputeAssignment> {
        val matched = filter(items) { candidate: ManualDisputeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDisputeAssignments(store: ManualDisputeAssignmentStore, query: ManualDisputeAssignmentQuery): MutableList<ManualDisputeAssignment> {
    return query.apply(store.list())
}
