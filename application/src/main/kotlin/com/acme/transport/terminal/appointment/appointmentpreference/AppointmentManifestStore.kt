package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment manifests by code. */
interface AppointmentManifestStore {
    fun find(code: String): AppointmentManifest?
    fun save(item: AppointmentManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentManifestStore(
    var items: MutableMap<String, AppointmentManifest>,
    var order: MutableList<String>,
) : AppointmentManifestStore {
    override fun find(code: String): AppointmentManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentManifest) {
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

    override fun list(): MutableList<AppointmentManifest> {
        val out = mutableListOf<AppointmentManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentManifestStore(): AppointmentManifestStore {
    return MemoryAppointmentManifestStore(mutableMapOf<String, AppointmentManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentManifestStore(
    var inner: AppointmentManifestStore,
    var cache: TtlCache<AppointmentManifest>,
    var hits: Int,
    var misses: Int,
) : AppointmentManifestStore {
    override fun find(code: String): AppointmentManifest? {
        val hit = (cache.get(code) as AppointmentManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentManifest> = inner.list()
}

fun newCachedAppointmentManifestStore(inner: AppointmentManifestStore, ttl: Int, clock: Clock): AppointmentManifestStore {
    return CachedAppointmentManifestStore(
        inner = inner,
        cache = newTtlCache<AppointmentManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment manifests. Empty fields match all. */
data class AppointmentManifestQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentManifest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentManifest>): MutableList<AppointmentManifest> {
        val matched = filter(items) { candidate: AppointmentManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentManifests(store: AppointmentManifestStore, query: AppointmentManifestQuery): MutableList<AppointmentManifest> {
    return query.apply(store.list())
}
