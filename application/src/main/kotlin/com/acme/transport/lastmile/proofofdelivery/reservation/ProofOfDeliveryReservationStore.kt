package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery reservations by code. */
interface ProofOfDeliveryReservationStore {
    fun find(code: String): ProofOfDeliveryReservation?
    fun save(item: ProofOfDeliveryReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryReservationStore(
    var items: MutableMap<String, ProofOfDeliveryReservation>,
    var order: MutableList<String>,
) : ProofOfDeliveryReservationStore {
    override fun find(code: String): ProofOfDeliveryReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryReservation) {
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

    override fun list(): MutableList<ProofOfDeliveryReservation> {
        val out = mutableListOf<ProofOfDeliveryReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryReservationStore(): ProofOfDeliveryReservationStore {
    return MemoryProofOfDeliveryReservationStore(
        mutableMapOf<String, ProofOfDeliveryReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryReservationStore(
    var inner: ProofOfDeliveryReservationStore,
    var cache: TtlCache<ProofOfDeliveryReservation>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryReservationStore {
    override fun find(code: String): ProofOfDeliveryReservation? {
        val hit = (cache.get(code) as ProofOfDeliveryReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryReservation> = inner.list()
}

fun newCachedProofOfDeliveryReservationStore(inner: ProofOfDeliveryReservationStore, ttl: Int, clock: Clock): ProofOfDeliveryReservationStore {
    return CachedProofOfDeliveryReservationStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery reservations. Empty fields match all. */
data class ProofOfDeliveryReservationQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryReservation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<ProofOfDeliveryReservation>): MutableList<ProofOfDeliveryReservation> {
        val matched = filter(items) { candidate: ProofOfDeliveryReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryReservations(store: ProofOfDeliveryReservationStore, query: ProofOfDeliveryReservationQuery): MutableList<ProofOfDeliveryReservation> {
    return query.apply(store.list())
}
