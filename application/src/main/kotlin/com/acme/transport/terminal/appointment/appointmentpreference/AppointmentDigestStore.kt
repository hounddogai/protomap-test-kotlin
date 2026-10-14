package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment digests by code. */
interface AppointmentDigestStore {
    fun find(code: String): AppointmentDigest?
    fun save(item: AppointmentDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentDigestStore(
    var items: MutableMap<String, AppointmentDigest>,
    var order: MutableList<String>,
) : AppointmentDigestStore {
    override fun find(code: String): AppointmentDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentDigest) {
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

    override fun list(): MutableList<AppointmentDigest> {
        val out = mutableListOf<AppointmentDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentDigestStore(): AppointmentDigestStore {
    return MemoryAppointmentDigestStore(mutableMapOf<String, AppointmentDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentDigestStore(
    var inner: AppointmentDigestStore,
    var cache: TtlCache<AppointmentDigest>,
    var hits: Int,
    var misses: Int,
) : AppointmentDigestStore {
    override fun find(code: String): AppointmentDigest? {
        val hit = (cache.get(code) as AppointmentDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentDigest> = inner.list()
}

fun newCachedAppointmentDigestStore(inner: AppointmentDigestStore, ttl: Int, clock: Clock): AppointmentDigestStore {
    return CachedAppointmentDigestStore(
        inner = inner,
        cache = newTtlCache<AppointmentDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment digests. Empty fields match all. */
data class AppointmentDigestQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<AppointmentDigest>): MutableList<AppointmentDigest> {
        val matched = filter(items) { candidate: AppointmentDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentDigests(store: AppointmentDigestStore, query: AppointmentDigestQuery): MutableList<AppointmentDigest> {
    return query.apply(store.list())
}
