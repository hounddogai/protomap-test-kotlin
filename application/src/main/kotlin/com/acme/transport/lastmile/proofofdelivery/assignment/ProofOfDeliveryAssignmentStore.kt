package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery assignments by code. */
interface ProofOfDeliveryAssignmentStore {
    fun find(code: String): ProofOfDeliveryAssignment?
    fun save(item: ProofOfDeliveryAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryAssignmentStore(
    var items: MutableMap<String, ProofOfDeliveryAssignment>,
    var order: MutableList<String>,
) : ProofOfDeliveryAssignmentStore {
    override fun find(code: String): ProofOfDeliveryAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryAssignment) {
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

    override fun list(): MutableList<ProofOfDeliveryAssignment> {
        val out = mutableListOf<ProofOfDeliveryAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryAssignmentStore(): ProofOfDeliveryAssignmentStore {
    return MemoryProofOfDeliveryAssignmentStore(
        mutableMapOf<String, ProofOfDeliveryAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryAssignmentStore(
    var inner: ProofOfDeliveryAssignmentStore,
    var cache: TtlCache<ProofOfDeliveryAssignment>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryAssignmentStore {
    override fun find(code: String): ProofOfDeliveryAssignment? {
        val hit = (cache.get(code) as ProofOfDeliveryAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryAssignment> = inner.list()
}

fun newCachedProofOfDeliveryAssignmentStore(inner: ProofOfDeliveryAssignmentStore, ttl: Int, clock: Clock): ProofOfDeliveryAssignmentStore {
    return CachedProofOfDeliveryAssignmentStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery assignments. Empty fields match all. */
data class ProofOfDeliveryAssignmentQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryAssignment>): MutableList<ProofOfDeliveryAssignment> {
        val matched = filter(items) { candidate: ProofOfDeliveryAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryAssignments(store: ProofOfDeliveryAssignmentStore, query: ProofOfDeliveryAssignmentQuery): MutableList<ProofOfDeliveryAssignment> {
    return query.apply(store.list())
}
