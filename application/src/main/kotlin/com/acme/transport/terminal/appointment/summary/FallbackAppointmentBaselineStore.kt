package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback appointment baselines by code. */
interface FallbackAppointmentBaselineStore {
    fun find(code: String): FallbackAppointmentBaseline?
    fun save(item: FallbackAppointmentBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAppointmentBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAppointmentBaselineStore(
    var items: MutableMap<String, FallbackAppointmentBaseline>,
    var order: MutableList<String>,
) : FallbackAppointmentBaselineStore {
    override fun find(code: String): FallbackAppointmentBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAppointmentBaseline) {
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

    override fun list(): MutableList<FallbackAppointmentBaseline> {
        val out = mutableListOf<FallbackAppointmentBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAppointmentBaselineStore(): FallbackAppointmentBaselineStore {
    return MemoryFallbackAppointmentBaselineStore(
        mutableMapOf<String, FallbackAppointmentBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAppointmentBaselineStore(
    var inner: FallbackAppointmentBaselineStore,
    var cache: TtlCache<FallbackAppointmentBaseline>,
    var hits: Int,
    var misses: Int,
) : FallbackAppointmentBaselineStore {
    override fun find(code: String): FallbackAppointmentBaseline? {
        val hit = (cache.get(code) as FallbackAppointmentBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAppointmentBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAppointmentBaseline> = inner.list()
}

fun newCachedFallbackAppointmentBaselineStore(inner: FallbackAppointmentBaselineStore, ttl: Int, clock: Clock): FallbackAppointmentBaselineStore {
    return CachedFallbackAppointmentBaselineStore(
        inner = inner,
        cache = newTtlCache<FallbackAppointmentBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback appointment baselines. Empty fields match all. */
data class FallbackAppointmentBaselineQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAppointmentBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAppointmentBaseline>): MutableList<FallbackAppointmentBaseline> {
        val matched = filter(items) { candidate: FallbackAppointmentBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAppointmentBaselines(store: FallbackAppointmentBaselineStore, query: FallbackAppointmentBaselineQuery): MutableList<FallbackAppointmentBaseline> {
    return query.apply(store.list())
}
