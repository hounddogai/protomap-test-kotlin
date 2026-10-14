package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy appointment overrides by code. */
interface LegacyAppointmentOverrideStore {
    fun find(code: String): LegacyAppointmentOverride?
    fun save(item: LegacyAppointmentOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAppointmentOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAppointmentOverrideStore(
    var items: MutableMap<String, LegacyAppointmentOverride>,
    var order: MutableList<String>,
) : LegacyAppointmentOverrideStore {
    override fun find(code: String): LegacyAppointmentOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAppointmentOverride) {
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

    override fun list(): MutableList<LegacyAppointmentOverride> {
        val out = mutableListOf<LegacyAppointmentOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAppointmentOverrideStore(): LegacyAppointmentOverrideStore {
    return MemoryLegacyAppointmentOverrideStore(
        mutableMapOf<String, LegacyAppointmentOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAppointmentOverrideStore(
    var inner: LegacyAppointmentOverrideStore,
    var cache: TtlCache<LegacyAppointmentOverride>,
    var hits: Int,
    var misses: Int,
) : LegacyAppointmentOverrideStore {
    override fun find(code: String): LegacyAppointmentOverride? {
        val hit = (cache.get(code) as LegacyAppointmentOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAppointmentOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAppointmentOverride> = inner.list()
}

fun newCachedLegacyAppointmentOverrideStore(inner: LegacyAppointmentOverrideStore, ttl: Int, clock: Clock): LegacyAppointmentOverrideStore {
    return CachedLegacyAppointmentOverrideStore(
        inner = inner,
        cache = newTtlCache<LegacyAppointmentOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy appointment overrides. Empty fields match all. */
data class LegacyAppointmentOverrideQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAppointmentOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LegacyAppointmentOverride>): MutableList<LegacyAppointmentOverride> {
        val matched = filter(items) { candidate: LegacyAppointmentOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAppointmentOverrides(store: LegacyAppointmentOverrideStore, query: LegacyAppointmentOverrideQuery): MutableList<LegacyAppointmentOverride> {
    return query.apply(store.list())
}
