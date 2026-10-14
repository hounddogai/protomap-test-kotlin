package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending declaration reservations by code. */
interface PendingDeclarationReservationStore {
    fun find(code: String): PendingDeclarationReservation?
    fun save(item: PendingDeclarationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDeclarationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDeclarationReservationStore(
    var items: MutableMap<String, PendingDeclarationReservation>,
    var order: MutableList<String>,
) : PendingDeclarationReservationStore {
    override fun find(code: String): PendingDeclarationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDeclarationReservation) {
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

    override fun list(): MutableList<PendingDeclarationReservation> {
        val out = mutableListOf<PendingDeclarationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDeclarationReservationStore(): PendingDeclarationReservationStore {
    return MemoryPendingDeclarationReservationStore(
        mutableMapOf<String, PendingDeclarationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDeclarationReservationStore(
    var inner: PendingDeclarationReservationStore,
    var cache: TtlCache<PendingDeclarationReservation>,
    var hits: Int,
    var misses: Int,
) : PendingDeclarationReservationStore {
    override fun find(code: String): PendingDeclarationReservation? {
        val hit = (cache.get(code) as PendingDeclarationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDeclarationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDeclarationReservation> = inner.list()
}

fun newCachedPendingDeclarationReservationStore(inner: PendingDeclarationReservationStore, ttl: Int, clock: Clock): PendingDeclarationReservationStore {
    return CachedPendingDeclarationReservationStore(
        inner = inner,
        cache = newTtlCache<PendingDeclarationReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending declaration reservations. Empty fields match all. */
data class PendingDeclarationReservationQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDeclarationReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDeclarationReservation>): MutableList<PendingDeclarationReservation> {
        val matched = filter(items) { candidate: PendingDeclarationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDeclarationReservations(store: PendingDeclarationReservationStore, query: PendingDeclarationReservationQuery): MutableList<PendingDeclarationReservation> {
    return query.apply(store.list())
}
