package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender cycles by code. */
interface TenderCycleStore {
    fun find(code: String): TenderCycle?
    fun save(item: TenderCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderCycleStore(
    var items: MutableMap<String, TenderCycle>,
    var order: MutableList<String>,
) : TenderCycleStore {
    override fun find(code: String): TenderCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderCycle) {
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

    override fun list(): MutableList<TenderCycle> {
        val out = mutableListOf<TenderCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderCycleStore(): TenderCycleStore {
    return MemoryTenderCycleStore(mutableMapOf<String, TenderCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderCycleStore(
    var inner: TenderCycleStore,
    var cache: TtlCache<TenderCycle>,
    var hits: Int,
    var misses: Int,
) : TenderCycleStore {
    override fun find(code: String): TenderCycle? {
        val hit = (cache.get(code) as TenderCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderCycle> = inner.list()
}

fun newCachedTenderCycleStore(inner: TenderCycleStore, ttl: Int, clock: Clock): TenderCycleStore {
    return CachedTenderCycleStore(
        inner = inner,
        cache = newTtlCache<TenderCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender cycles. Empty fields match all. */
data class TenderCycleQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderCycle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<TenderCycle>): MutableList<TenderCycle> {
        val matched = filter(items) { candidate: TenderCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderCycles(store: TenderCycleStore, query: TenderCycleQuery): MutableList<TenderCycle> {
    return query.apply(store.list())
}
