package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment claims by code. */
interface AppointmentClaimStore {
    fun find(code: String): AppointmentClaim?
    fun save(item: AppointmentClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentClaimStore(
    var items: MutableMap<String, AppointmentClaim>,
    var order: MutableList<String>,
) : AppointmentClaimStore {
    override fun find(code: String): AppointmentClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentClaim) {
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

    override fun list(): MutableList<AppointmentClaim> {
        val out = mutableListOf<AppointmentClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentClaimStore(): AppointmentClaimStore {
    return MemoryAppointmentClaimStore(mutableMapOf<String, AppointmentClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentClaimStore(
    var inner: AppointmentClaimStore,
    var cache: TtlCache<AppointmentClaim>,
    var hits: Int,
    var misses: Int,
) : AppointmentClaimStore {
    override fun find(code: String): AppointmentClaim? {
        val hit = (cache.get(code) as AppointmentClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentClaim> = inner.list()
}

fun newCachedAppointmentClaimStore(inner: AppointmentClaimStore, ttl: Int, clock: Clock): AppointmentClaimStore {
    return CachedAppointmentClaimStore(
        inner = inner,
        cache = newTtlCache<AppointmentClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment claims. Empty fields match all. */
data class AppointmentClaimQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentClaim>): MutableList<AppointmentClaim> {
        val matched = filter(items) { candidate: AppointmentClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentClaims(store: AppointmentClaimStore, query: AppointmentClaimQuery): MutableList<AppointmentClaim> {
    return query.apply(store.list())
}
