package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient proof of delivery reservations by code. */
interface LenientProofOfDeliveryReservationStore {
    fun find(code: String): LenientProofOfDeliveryReservation?
    fun save(item: LenientProofOfDeliveryReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientProofOfDeliveryReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientProofOfDeliveryReservationStore(
    var items: MutableMap<String, LenientProofOfDeliveryReservation>,
    var order: MutableList<String>,
) : LenientProofOfDeliveryReservationStore {
    override fun find(code: String): LenientProofOfDeliveryReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientProofOfDeliveryReservation) {
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

    override fun list(): MutableList<LenientProofOfDeliveryReservation> {
        val out = mutableListOf<LenientProofOfDeliveryReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientProofOfDeliveryReservationStore(): LenientProofOfDeliveryReservationStore {
    return MemoryLenientProofOfDeliveryReservationStore(
        mutableMapOf<String, LenientProofOfDeliveryReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientProofOfDeliveryReservationStore(
    var inner: LenientProofOfDeliveryReservationStore,
    var cache: TtlCache<LenientProofOfDeliveryReservation>,
    var hits: Int,
    var misses: Int,
) : LenientProofOfDeliveryReservationStore {
    override fun find(code: String): LenientProofOfDeliveryReservation? {
        val hit = (cache.get(code) as LenientProofOfDeliveryReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientProofOfDeliveryReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientProofOfDeliveryReservation> = inner.list()
}

fun newCachedLenientProofOfDeliveryReservationStore(inner: LenientProofOfDeliveryReservationStore, ttl: Int, clock: Clock): LenientProofOfDeliveryReservationStore {
    return CachedLenientProofOfDeliveryReservationStore(
        inner = inner,
        cache = newTtlCache<LenientProofOfDeliveryReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient proof of delivery reservations. Empty fields match all. */
data class LenientProofOfDeliveryReservationQuery(
    var region: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientProofOfDeliveryReservation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientProofOfDeliveryReservation>): MutableList<LenientProofOfDeliveryReservation> {
        val matched = filter(items) { candidate: LenientProofOfDeliveryReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientProofOfDeliveryReservations(store: LenientProofOfDeliveryReservationStore, query: LenientProofOfDeliveryReservationQuery): MutableList<LenientProofOfDeliveryReservation> {
    return query.apply(store.list())
}
