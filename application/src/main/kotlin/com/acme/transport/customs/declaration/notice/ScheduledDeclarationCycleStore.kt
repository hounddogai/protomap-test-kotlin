package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled declaration cycles by code. */
interface ScheduledDeclarationCycleStore {
    fun find(code: String): ScheduledDeclarationCycle?
    fun save(item: ScheduledDeclarationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDeclarationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDeclarationCycleStore(
    var items: MutableMap<String, ScheduledDeclarationCycle>,
    var order: MutableList<String>,
) : ScheduledDeclarationCycleStore {
    override fun find(code: String): ScheduledDeclarationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDeclarationCycle) {
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

    override fun list(): MutableList<ScheduledDeclarationCycle> {
        val out = mutableListOf<ScheduledDeclarationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDeclarationCycleStore(): ScheduledDeclarationCycleStore {
    return MemoryScheduledDeclarationCycleStore(
        mutableMapOf<String, ScheduledDeclarationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDeclarationCycleStore(
    var inner: ScheduledDeclarationCycleStore,
    var cache: TtlCache<ScheduledDeclarationCycle>,
    var hits: Int,
    var misses: Int,
) : ScheduledDeclarationCycleStore {
    override fun find(code: String): ScheduledDeclarationCycle? {
        val hit = (cache.get(code) as ScheduledDeclarationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDeclarationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDeclarationCycle> = inner.list()
}

fun newCachedScheduledDeclarationCycleStore(inner: ScheduledDeclarationCycleStore, ttl: Int, clock: Clock): ScheduledDeclarationCycleStore {
    return CachedScheduledDeclarationCycleStore(
        inner = inner,
        cache = newTtlCache<ScheduledDeclarationCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled declaration cycles. Empty fields match all. */
data class ScheduledDeclarationCycleQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDeclarationCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDeclarationCycle>): MutableList<ScheduledDeclarationCycle> {
        val matched = filter(items) { candidate: ScheduledDeclarationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDeclarationCycles(store: ScheduledDeclarationCycleStore, query: ScheduledDeclarationCycleQuery): MutableList<ScheduledDeclarationCycle> {
    return query.apply(store.list())
}
