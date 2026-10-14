package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient appointment rollups by code. */
interface LenientAppointmentRollupStore {
    fun find(code: String): LenientAppointmentRollup?
    fun save(item: LenientAppointmentRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAppointmentRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAppointmentRollupStore(
    var items: MutableMap<String, LenientAppointmentRollup>,
    var order: MutableList<String>,
) : LenientAppointmentRollupStore {
    override fun find(code: String): LenientAppointmentRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAppointmentRollup) {
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

    override fun list(): MutableList<LenientAppointmentRollup> {
        val out = mutableListOf<LenientAppointmentRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAppointmentRollupStore(): LenientAppointmentRollupStore {
    return MemoryLenientAppointmentRollupStore(
        mutableMapOf<String, LenientAppointmentRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAppointmentRollupStore(
    var inner: LenientAppointmentRollupStore,
    var cache: TtlCache<LenientAppointmentRollup>,
    var hits: Int,
    var misses: Int,
) : LenientAppointmentRollupStore {
    override fun find(code: String): LenientAppointmentRollup? {
        val hit = (cache.get(code) as LenientAppointmentRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAppointmentRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAppointmentRollup> = inner.list()
}

fun newCachedLenientAppointmentRollupStore(inner: LenientAppointmentRollupStore, ttl: Int, clock: Clock): LenientAppointmentRollupStore {
    return CachedLenientAppointmentRollupStore(
        inner = inner,
        cache = newTtlCache<LenientAppointmentRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient appointment rollups. Empty fields match all. */
data class LenientAppointmentRollupQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAppointmentRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LenientAppointmentRollup>): MutableList<LenientAppointmentRollup> {
        val matched = filter(items) { candidate: LenientAppointmentRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAppointmentRollups(store: LenientAppointmentRollupStore, query: LenientAppointmentRollupQuery): MutableList<LenientAppointmentRollup> {
    return query.apply(store.list())
}
