package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive declaration reservations by code. */
interface AdaptiveDeclarationReservationStore {
    fun find(code: String): AdaptiveDeclarationReservation?
    fun save(item: AdaptiveDeclarationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDeclarationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDeclarationReservationStore(
    var items: MutableMap<String, AdaptiveDeclarationReservation>,
    var order: MutableList<String>,
) : AdaptiveDeclarationReservationStore {
    override fun find(code: String): AdaptiveDeclarationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDeclarationReservation) {
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

    override fun list(): MutableList<AdaptiveDeclarationReservation> {
        val out = mutableListOf<AdaptiveDeclarationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDeclarationReservationStore(): AdaptiveDeclarationReservationStore {
    return MemoryAdaptiveDeclarationReservationStore(
        mutableMapOf<String, AdaptiveDeclarationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDeclarationReservationStore(
    var inner: AdaptiveDeclarationReservationStore,
    var cache: TtlCache<AdaptiveDeclarationReservation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDeclarationReservationStore {
    override fun find(code: String): AdaptiveDeclarationReservation? {
        val hit = (cache.get(code) as AdaptiveDeclarationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDeclarationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDeclarationReservation> = inner.list()
}

fun newCachedAdaptiveDeclarationReservationStore(inner: AdaptiveDeclarationReservationStore, ttl: Int, clock: Clock): AdaptiveDeclarationReservationStore {
    return CachedAdaptiveDeclarationReservationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDeclarationReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive declaration reservations. Empty fields match all. */
data class AdaptiveDeclarationReservationQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDeclarationReservation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDeclarationReservation>): MutableList<AdaptiveDeclarationReservation> {
        val matched = filter(items) { candidate: AdaptiveDeclarationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDeclarationReservations(store: AdaptiveDeclarationReservationStore, query: AdaptiveDeclarationReservationQuery): MutableList<AdaptiveDeclarationReservation> {
    return query.apply(store.list())
}
