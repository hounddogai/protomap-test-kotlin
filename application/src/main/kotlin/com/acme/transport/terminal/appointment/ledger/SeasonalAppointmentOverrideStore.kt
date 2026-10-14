package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal appointment overrides by code. */
interface SeasonalAppointmentOverrideStore {
    fun find(code: String): SeasonalAppointmentOverride?
    fun save(item: SeasonalAppointmentOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAppointmentOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAppointmentOverrideStore(
    var items: MutableMap<String, SeasonalAppointmentOverride>,
    var order: MutableList<String>,
) : SeasonalAppointmentOverrideStore {
    override fun find(code: String): SeasonalAppointmentOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAppointmentOverride) {
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

    override fun list(): MutableList<SeasonalAppointmentOverride> {
        val out = mutableListOf<SeasonalAppointmentOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAppointmentOverrideStore(): SeasonalAppointmentOverrideStore {
    return MemorySeasonalAppointmentOverrideStore(
        mutableMapOf<String, SeasonalAppointmentOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAppointmentOverrideStore(
    var inner: SeasonalAppointmentOverrideStore,
    var cache: TtlCache<SeasonalAppointmentOverride>,
    var hits: Int,
    var misses: Int,
) : SeasonalAppointmentOverrideStore {
    override fun find(code: String): SeasonalAppointmentOverride? {
        val hit = (cache.get(code) as SeasonalAppointmentOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAppointmentOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAppointmentOverride> = inner.list()
}

fun newCachedSeasonalAppointmentOverrideStore(inner: SeasonalAppointmentOverrideStore, ttl: Int, clock: Clock): SeasonalAppointmentOverrideStore {
    return CachedSeasonalAppointmentOverrideStore(
        inner = inner,
        cache = newTtlCache<SeasonalAppointmentOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal appointment overrides. Empty fields match all. */
data class SeasonalAppointmentOverrideQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAppointmentOverride): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAppointmentOverride>): MutableList<SeasonalAppointmentOverride> {
        val matched = filter(items) { candidate: SeasonalAppointmentOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAppointmentOverrides(store: SeasonalAppointmentOverrideStore, query: SeasonalAppointmentOverrideQuery): MutableList<SeasonalAppointmentOverride> {
    return query.apply(store.list())
}
