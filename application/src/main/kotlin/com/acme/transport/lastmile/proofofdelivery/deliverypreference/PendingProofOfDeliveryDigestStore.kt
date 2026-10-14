package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending proof of delivery digests by code. */
interface PendingProofOfDeliveryDigestStore {
    fun find(code: String): PendingProofOfDeliveryDigest?
    fun save(item: PendingProofOfDeliveryDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingProofOfDeliveryDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingProofOfDeliveryDigestStore(
    var items: MutableMap<String, PendingProofOfDeliveryDigest>,
    var order: MutableList<String>,
) : PendingProofOfDeliveryDigestStore {
    override fun find(code: String): PendingProofOfDeliveryDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingProofOfDeliveryDigest) {
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

    override fun list(): MutableList<PendingProofOfDeliveryDigest> {
        val out = mutableListOf<PendingProofOfDeliveryDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingProofOfDeliveryDigestStore(): PendingProofOfDeliveryDigestStore {
    return MemoryPendingProofOfDeliveryDigestStore(
        mutableMapOf<String, PendingProofOfDeliveryDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingProofOfDeliveryDigestStore(
    var inner: PendingProofOfDeliveryDigestStore,
    var cache: TtlCache<PendingProofOfDeliveryDigest>,
    var hits: Int,
    var misses: Int,
) : PendingProofOfDeliveryDigestStore {
    override fun find(code: String): PendingProofOfDeliveryDigest? {
        val hit = (cache.get(code) as PendingProofOfDeliveryDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingProofOfDeliveryDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingProofOfDeliveryDigest> = inner.list()
}

fun newCachedPendingProofOfDeliveryDigestStore(inner: PendingProofOfDeliveryDigestStore, ttl: Int, clock: Clock): PendingProofOfDeliveryDigestStore {
    return CachedPendingProofOfDeliveryDigestStore(
        inner = inner,
        cache = newTtlCache<PendingProofOfDeliveryDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending proof of delivery digests. Empty fields match all. */
data class PendingProofOfDeliveryDigestQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingProofOfDeliveryDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingProofOfDeliveryDigest>): MutableList<PendingProofOfDeliveryDigest> {
        val matched = filter(items) { candidate: PendingProofOfDeliveryDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingProofOfDeliveryDigests(store: PendingProofOfDeliveryDigestStore, query: PendingProofOfDeliveryDigestQuery): MutableList<PendingProofOfDeliveryDigest> {
    return query.apply(store.list())
}
