package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default appointment manifests by code. */
interface DefaultAppointmentManifestStore {
    fun find(code: String): DefaultAppointmentManifest?
    fun save(item: DefaultAppointmentManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAppointmentManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAppointmentManifestStore(
    var items: MutableMap<String, DefaultAppointmentManifest>,
    var order: MutableList<String>,
) : DefaultAppointmentManifestStore {
    override fun find(code: String): DefaultAppointmentManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAppointmentManifest) {
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

    override fun list(): MutableList<DefaultAppointmentManifest> {
        val out = mutableListOf<DefaultAppointmentManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAppointmentManifestStore(): DefaultAppointmentManifestStore {
    return MemoryDefaultAppointmentManifestStore(
        mutableMapOf<String, DefaultAppointmentManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAppointmentManifestStore(
    var inner: DefaultAppointmentManifestStore,
    var cache: TtlCache<DefaultAppointmentManifest>,
    var hits: Int,
    var misses: Int,
) : DefaultAppointmentManifestStore {
    override fun find(code: String): DefaultAppointmentManifest? {
        val hit = (cache.get(code) as DefaultAppointmentManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAppointmentManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAppointmentManifest> = inner.list()
}

fun newCachedDefaultAppointmentManifestStore(inner: DefaultAppointmentManifestStore, ttl: Int, clock: Clock): DefaultAppointmentManifestStore {
    return CachedDefaultAppointmentManifestStore(
        inner = inner,
        cache = newTtlCache<DefaultAppointmentManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default appointment manifests. Empty fields match all. */
data class DefaultAppointmentManifestQuery(
    var category: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAppointmentManifest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAppointmentManifest>): MutableList<DefaultAppointmentManifest> {
        val matched = filter(items) { candidate: DefaultAppointmentManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAppointmentManifests(store: DefaultAppointmentManifestStore, query: DefaultAppointmentManifestQuery): MutableList<DefaultAppointmentManifest> {
    return query.apply(store.list())
}
