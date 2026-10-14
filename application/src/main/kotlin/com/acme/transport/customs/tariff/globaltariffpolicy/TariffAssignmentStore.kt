package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff assignments by code. */
interface TariffAssignmentStore {
    fun find(code: String): TariffAssignment?
    fun save(item: TariffAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffAssignmentStore(
    var items: MutableMap<String, TariffAssignment>,
    var order: MutableList<String>,
) : TariffAssignmentStore {
    override fun find(code: String): TariffAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffAssignment) {
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

    override fun list(): MutableList<TariffAssignment> {
        val out = mutableListOf<TariffAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffAssignmentStore(): TariffAssignmentStore {
    return MemoryTariffAssignmentStore(mutableMapOf<String, TariffAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffAssignmentStore(
    var inner: TariffAssignmentStore,
    var cache: TtlCache<TariffAssignment>,
    var hits: Int,
    var misses: Int,
) : TariffAssignmentStore {
    override fun find(code: String): TariffAssignment? {
        val hit = (cache.get(code) as TariffAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffAssignment> = inner.list()
}

fun newCachedTariffAssignmentStore(inner: TariffAssignmentStore, ttl: Int, clock: Clock): TariffAssignmentStore {
    return CachedTariffAssignmentStore(
        inner = inner,
        cache = newTtlCache<TariffAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff assignments. Empty fields match all. */
data class TariffAssignmentQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffAssignment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffAssignment>): MutableList<TariffAssignment> {
        val matched = filter(items) { candidate: TariffAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffAssignments(store: TariffAssignmentStore, query: TariffAssignmentQuery): MutableList<TariffAssignment> {
    return query.apply(store.list())
}
