package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback appointment bundles by code. */
interface FallbackAppointmentBundleStore {
    fun find(code: String): FallbackAppointmentBundle?
    fun save(item: FallbackAppointmentBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAppointmentBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAppointmentBundleStore(
    var items: MutableMap<String, FallbackAppointmentBundle>,
    var order: MutableList<String>,
) : FallbackAppointmentBundleStore {
    override fun find(code: String): FallbackAppointmentBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAppointmentBundle) {
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

    override fun list(): MutableList<FallbackAppointmentBundle> {
        val out = mutableListOf<FallbackAppointmentBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAppointmentBundleStore(): FallbackAppointmentBundleStore {
    return MemoryFallbackAppointmentBundleStore(
        mutableMapOf<String, FallbackAppointmentBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAppointmentBundleStore(
    var inner: FallbackAppointmentBundleStore,
    var cache: TtlCache<FallbackAppointmentBundle>,
    var hits: Int,
    var misses: Int,
) : FallbackAppointmentBundleStore {
    override fun find(code: String): FallbackAppointmentBundle? {
        val hit = (cache.get(code) as FallbackAppointmentBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAppointmentBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAppointmentBundle> = inner.list()
}

fun newCachedFallbackAppointmentBundleStore(inner: FallbackAppointmentBundleStore, ttl: Int, clock: Clock): FallbackAppointmentBundleStore {
    return CachedFallbackAppointmentBundleStore(
        inner = inner,
        cache = newTtlCache<FallbackAppointmentBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback appointment bundles. Empty fields match all. */
data class FallbackAppointmentBundleQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAppointmentBundle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAppointmentBundle>): MutableList<FallbackAppointmentBundle> {
        val matched = filter(items) { candidate: FallbackAppointmentBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAppointmentBundles(store: FallbackAppointmentBundleStore, query: FallbackAppointmentBundleQuery): MutableList<FallbackAppointmentBundle> {
    return query.apply(store.list())
}
