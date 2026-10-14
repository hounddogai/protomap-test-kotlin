package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled tender cycles by code. */
interface ScheduledTenderCycleStore {
    fun find(code: String): ScheduledTenderCycle?
    fun save(item: ScheduledTenderCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTenderCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTenderCycleStore(
    var items: MutableMap<String, ScheduledTenderCycle>,
    var order: MutableList<String>,
) : ScheduledTenderCycleStore {
    override fun find(code: String): ScheduledTenderCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTenderCycle) {
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

    override fun list(): MutableList<ScheduledTenderCycle> {
        val out = mutableListOf<ScheduledTenderCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTenderCycleStore(): ScheduledTenderCycleStore {
    return MemoryScheduledTenderCycleStore(
        mutableMapOf<String, ScheduledTenderCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTenderCycleStore(
    var inner: ScheduledTenderCycleStore,
    var cache: TtlCache<ScheduledTenderCycle>,
    var hits: Int,
    var misses: Int,
) : ScheduledTenderCycleStore {
    override fun find(code: String): ScheduledTenderCycle? {
        val hit = (cache.get(code) as ScheduledTenderCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTenderCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTenderCycle> = inner.list()
}

fun newCachedScheduledTenderCycleStore(inner: ScheduledTenderCycleStore, ttl: Int, clock: Clock): ScheduledTenderCycleStore {
    return CachedScheduledTenderCycleStore(
        inner = inner,
        cache = newTtlCache<ScheduledTenderCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled tender cycles. Empty fields match all. */
data class ScheduledTenderCycleQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTenderCycle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTenderCycle>): MutableList<ScheduledTenderCycle> {
        val matched = filter(items) { candidate: ScheduledTenderCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTenderCycles(store: ScheduledTenderCycleStore, query: ScheduledTenderCycleQuery): MutableList<ScheduledTenderCycle> {
    return query.apply(store.list())
}
