package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient appointment mappings by code. */
interface LenientAppointmentMappingStore {
    fun find(code: String): LenientAppointmentMapping?
    fun save(item: LenientAppointmentMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAppointmentMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAppointmentMappingStore(
    var items: MutableMap<String, LenientAppointmentMapping>,
    var order: MutableList<String>,
) : LenientAppointmentMappingStore {
    override fun find(code: String): LenientAppointmentMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAppointmentMapping) {
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

    override fun list(): MutableList<LenientAppointmentMapping> {
        val out = mutableListOf<LenientAppointmentMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAppointmentMappingStore(): LenientAppointmentMappingStore {
    return MemoryLenientAppointmentMappingStore(
        mutableMapOf<String, LenientAppointmentMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAppointmentMappingStore(
    var inner: LenientAppointmentMappingStore,
    var cache: TtlCache<LenientAppointmentMapping>,
    var hits: Int,
    var misses: Int,
) : LenientAppointmentMappingStore {
    override fun find(code: String): LenientAppointmentMapping? {
        val hit = (cache.get(code) as LenientAppointmentMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAppointmentMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAppointmentMapping> = inner.list()
}

fun newCachedLenientAppointmentMappingStore(inner: LenientAppointmentMappingStore, ttl: Int, clock: Clock): LenientAppointmentMappingStore {
    return CachedLenientAppointmentMappingStore(
        inner = inner,
        cache = newTtlCache<LenientAppointmentMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient appointment mappings. Empty fields match all. */
data class LenientAppointmentMappingQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAppointmentMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAppointmentMapping>): MutableList<LenientAppointmentMapping> {
        val matched = filter(items) { candidate: LenientAppointmentMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAppointmentMappings(store: LenientAppointmentMappingStore, query: LenientAppointmentMappingQuery): MutableList<LenientAppointmentMapping> {
    return query.apply(store.list())
}
