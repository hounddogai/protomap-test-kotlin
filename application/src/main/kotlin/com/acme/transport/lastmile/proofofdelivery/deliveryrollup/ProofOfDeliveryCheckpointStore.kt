package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery checkpoints by code. */
interface ProofOfDeliveryCheckpointStore {
    fun find(code: String): ProofOfDeliveryCheckpoint?
    fun save(item: ProofOfDeliveryCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryCheckpointStore(
    var items: MutableMap<String, ProofOfDeliveryCheckpoint>,
    var order: MutableList<String>,
) : ProofOfDeliveryCheckpointStore {
    override fun find(code: String): ProofOfDeliveryCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryCheckpoint) {
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

    override fun list(): MutableList<ProofOfDeliveryCheckpoint> {
        val out = mutableListOf<ProofOfDeliveryCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryCheckpointStore(): ProofOfDeliveryCheckpointStore {
    return MemoryProofOfDeliveryCheckpointStore(
        mutableMapOf<String, ProofOfDeliveryCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryCheckpointStore(
    var inner: ProofOfDeliveryCheckpointStore,
    var cache: TtlCache<ProofOfDeliveryCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryCheckpointStore {
    override fun find(code: String): ProofOfDeliveryCheckpoint? {
        val hit = (cache.get(code) as ProofOfDeliveryCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryCheckpoint> = inner.list()
}

fun newCachedProofOfDeliveryCheckpointStore(inner: ProofOfDeliveryCheckpointStore, ttl: Int, clock: Clock): ProofOfDeliveryCheckpointStore {
    return CachedProofOfDeliveryCheckpointStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery checkpoints. Empty fields match all. */
data class ProofOfDeliveryCheckpointQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryCheckpoint>): MutableList<ProofOfDeliveryCheckpoint> {
        val matched = filter(items) { candidate: ProofOfDeliveryCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryCheckpoints(store: ProofOfDeliveryCheckpointStore, query: ProofOfDeliveryCheckpointQuery): MutableList<ProofOfDeliveryCheckpoint> {
    return query.apply(store.list())
}
