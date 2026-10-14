package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy appointment bundles by code. */
interface LegacyAppointmentBundleStore {
    fun find(code: String): LegacyAppointmentBundle?
    fun save(item: LegacyAppointmentBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAppointmentBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAppointmentBundleStore(
    var items: MutableMap<String, LegacyAppointmentBundle>,
    var order: MutableList<String>,
) : LegacyAppointmentBundleStore {
    override fun find(code: String): LegacyAppointmentBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAppointmentBundle) {
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

    override fun list(): MutableList<LegacyAppointmentBundle> {
        val out = mutableListOf<LegacyAppointmentBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAppointmentBundleStore(): LegacyAppointmentBundleStore {
    return MemoryLegacyAppointmentBundleStore(
        mutableMapOf<String, LegacyAppointmentBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAppointmentBundleStore(
    var inner: LegacyAppointmentBundleStore,
    var cache: TtlCache<LegacyAppointmentBundle>,
    var hits: Int,
    var misses: Int,
) : LegacyAppointmentBundleStore {
    override fun find(code: String): LegacyAppointmentBundle? {
        val hit = (cache.get(code) as LegacyAppointmentBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAppointmentBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAppointmentBundle> = inner.list()
}

fun newCachedLegacyAppointmentBundleStore(inner: LegacyAppointmentBundleStore, ttl: Int, clock: Clock): LegacyAppointmentBundleStore {
    return CachedLegacyAppointmentBundleStore(
        inner = inner,
        cache = newTtlCache<LegacyAppointmentBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy appointment bundles. Empty fields match all. */
data class LegacyAppointmentBundleQuery(
    var region: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAppointmentBundle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAppointmentBundle>): MutableList<LegacyAppointmentBundle> {
        val matched = filter(items) { candidate: LegacyAppointmentBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAppointmentBundles(store: LegacyAppointmentBundleStore, query: LegacyAppointmentBundleQuery): MutableList<LegacyAppointmentBundle> {
    return query.apply(store.list())
}
