package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled proof of delivery digests by code. */
interface ScheduledProofOfDeliveryDigestStore {
    fun find(code: String): ScheduledProofOfDeliveryDigest?
    fun save(item: ScheduledProofOfDeliveryDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledProofOfDeliveryDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledProofOfDeliveryDigestStore(
    var items: MutableMap<String, ScheduledProofOfDeliveryDigest>,
    var order: MutableList<String>,
) : ScheduledProofOfDeliveryDigestStore {
    override fun find(code: String): ScheduledProofOfDeliveryDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledProofOfDeliveryDigest) {
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

    override fun list(): MutableList<ScheduledProofOfDeliveryDigest> {
        val out = mutableListOf<ScheduledProofOfDeliveryDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledProofOfDeliveryDigestStore(): ScheduledProofOfDeliveryDigestStore {
    return MemoryScheduledProofOfDeliveryDigestStore(
        mutableMapOf<String, ScheduledProofOfDeliveryDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledProofOfDeliveryDigestStore(
    var inner: ScheduledProofOfDeliveryDigestStore,
    var cache: TtlCache<ScheduledProofOfDeliveryDigest>,
    var hits: Int,
    var misses: Int,
) : ScheduledProofOfDeliveryDigestStore {
    override fun find(code: String): ScheduledProofOfDeliveryDigest? {
        val hit = (cache.get(code) as ScheduledProofOfDeliveryDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledProofOfDeliveryDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledProofOfDeliveryDigest> = inner.list()
}

fun newCachedScheduledProofOfDeliveryDigestStore(inner: ScheduledProofOfDeliveryDigestStore, ttl: Int, clock: Clock): ScheduledProofOfDeliveryDigestStore {
    return CachedScheduledProofOfDeliveryDigestStore(
        inner = inner,
        cache = newTtlCache<ScheduledProofOfDeliveryDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled proof of delivery digests. Empty fields match all. */
data class ScheduledProofOfDeliveryDigestQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledProofOfDeliveryDigest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<ScheduledProofOfDeliveryDigest>): MutableList<ScheduledProofOfDeliveryDigest> {
        val matched = filter(items) { candidate: ScheduledProofOfDeliveryDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledProofOfDeliveryDigests(store: ScheduledProofOfDeliveryDigestStore, query: ScheduledProofOfDeliveryDigestQuery): MutableList<ScheduledProofOfDeliveryDigest> {
    return query.apply(store.list())
}
