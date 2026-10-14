package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration reservations by code. */
interface DeclarationReservationStore {
    fun find(code: String): DeclarationReservation?
    fun save(item: DeclarationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationReservationStore(
    var items: MutableMap<String, DeclarationReservation>,
    var order: MutableList<String>,
) : DeclarationReservationStore {
    override fun find(code: String): DeclarationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationReservation) {
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

    override fun list(): MutableList<DeclarationReservation> {
        val out = mutableListOf<DeclarationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationReservationStore(): DeclarationReservationStore {
    return MemoryDeclarationReservationStore(
        mutableMapOf<String, DeclarationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationReservationStore(
    var inner: DeclarationReservationStore,
    var cache: TtlCache<DeclarationReservation>,
    var hits: Int,
    var misses: Int,
) : DeclarationReservationStore {
    override fun find(code: String): DeclarationReservation? {
        val hit = (cache.get(code) as DeclarationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationReservation> = inner.list()
}

fun newCachedDeclarationReservationStore(inner: DeclarationReservationStore, ttl: Int, clock: Clock): DeclarationReservationStore {
    return CachedDeclarationReservationStore(
        inner = inner,
        cache = newTtlCache<DeclarationReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration reservations. Empty fields match all. */
data class DeclarationReservationQuery(
    var locale: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationReservation>): MutableList<DeclarationReservation> {
        val matched = filter(items) { candidate: DeclarationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationReservations(store: DeclarationReservationStore, query: DeclarationReservationQuery): MutableList<DeclarationReservation> {
    return query.apply(store.list())
}
