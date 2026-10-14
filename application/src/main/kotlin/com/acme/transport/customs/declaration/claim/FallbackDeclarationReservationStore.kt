package com.acme.transport.customs.declaration.claim

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback declaration reservations by code. */
interface FallbackDeclarationReservationStore {
    fun find(code: String): FallbackDeclarationReservation?
    fun save(item: FallbackDeclarationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDeclarationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDeclarationReservationStore(
    var items: MutableMap<String, FallbackDeclarationReservation>,
    var order: MutableList<String>,
) : FallbackDeclarationReservationStore {
    override fun find(code: String): FallbackDeclarationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDeclarationReservation) {
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

    override fun list(): MutableList<FallbackDeclarationReservation> {
        val out = mutableListOf<FallbackDeclarationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDeclarationReservationStore(): FallbackDeclarationReservationStore {
    return MemoryFallbackDeclarationReservationStore(
        mutableMapOf<String, FallbackDeclarationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDeclarationReservationStore(
    var inner: FallbackDeclarationReservationStore,
    var cache: TtlCache<FallbackDeclarationReservation>,
    var hits: Int,
    var misses: Int,
) : FallbackDeclarationReservationStore {
    override fun find(code: String): FallbackDeclarationReservation? {
        val hit = (cache.get(code) as FallbackDeclarationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDeclarationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDeclarationReservation> = inner.list()
}

fun newCachedFallbackDeclarationReservationStore(inner: FallbackDeclarationReservationStore, ttl: Int, clock: Clock): FallbackDeclarationReservationStore {
    return CachedFallbackDeclarationReservationStore(
        inner = inner,
        cache = newTtlCache<FallbackDeclarationReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback declaration reservations. Empty fields match all. */
data class FallbackDeclarationReservationQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDeclarationReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDeclarationReservation>): MutableList<FallbackDeclarationReservation> {
        val matched = filter(items) { candidate: FallbackDeclarationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDeclarationReservations(store: FallbackDeclarationReservationStore, query: FallbackDeclarationReservationQuery): MutableList<FallbackDeclarationReservation> {
    return query.apply(store.list())
}
