package com.acme.transport.network.node.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling node reservations by code. */
interface RollingNodeReservationStore {
    fun find(code: String): RollingNodeReservation?
    fun save(item: RollingNodeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNodeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNodeReservationStore(
    var items: MutableMap<String, RollingNodeReservation>,
    var order: MutableList<String>,
) : RollingNodeReservationStore {
    override fun find(code: String): RollingNodeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNodeReservation) {
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

    override fun list(): MutableList<RollingNodeReservation> {
        val out = mutableListOf<RollingNodeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNodeReservationStore(): RollingNodeReservationStore {
    return MemoryRollingNodeReservationStore(
        mutableMapOf<String, RollingNodeReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNodeReservationStore(
    var inner: RollingNodeReservationStore,
    var cache: TtlCache<RollingNodeReservation>,
    var hits: Int,
    var misses: Int,
) : RollingNodeReservationStore {
    override fun find(code: String): RollingNodeReservation? {
        val hit = (cache.get(code) as RollingNodeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNodeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNodeReservation> = inner.list()
}

fun newCachedRollingNodeReservationStore(inner: RollingNodeReservationStore, ttl: Int, clock: Clock): RollingNodeReservationStore {
    return CachedRollingNodeReservationStore(
        inner = inner,
        cache = newTtlCache<RollingNodeReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling node reservations. Empty fields match all. */
data class RollingNodeReservationQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNodeReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RollingNodeReservation>): MutableList<RollingNodeReservation> {
        val matched = filter(items) { candidate: RollingNodeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNodeReservations(store: RollingNodeReservationStore, query: RollingNodeReservationQuery): MutableList<RollingNodeReservation> {
    return query.apply(store.list())
}
