package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped service plan entries by code. */
interface CappedServicePlanEntryStore {
    fun find(code: String): CappedServicePlanEntry?
    fun save(item: CappedServicePlanEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServicePlanEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServicePlanEntryStore(
    var items: MutableMap<String, CappedServicePlanEntry>,
    var order: MutableList<String>,
) : CappedServicePlanEntryStore {
    override fun find(code: String): CappedServicePlanEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServicePlanEntry) {
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

    override fun list(): MutableList<CappedServicePlanEntry> {
        val out = mutableListOf<CappedServicePlanEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServicePlanEntryStore(): CappedServicePlanEntryStore {
    return MemoryCappedServicePlanEntryStore(
        mutableMapOf<String, CappedServicePlanEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServicePlanEntryStore(
    var inner: CappedServicePlanEntryStore,
    var cache: TtlCache<CappedServicePlanEntry>,
    var hits: Int,
    var misses: Int,
) : CappedServicePlanEntryStore {
    override fun find(code: String): CappedServicePlanEntry? {
        val hit = (cache.get(code) as CappedServicePlanEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServicePlanEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServicePlanEntry> = inner.list()
}

fun newCachedCappedServicePlanEntryStore(inner: CappedServicePlanEntryStore, ttl: Int, clock: Clock): CappedServicePlanEntryStore {
    return CachedCappedServicePlanEntryStore(
        inner = inner,
        cache = newTtlCache<CappedServicePlanEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service plan entries. Empty fields match all. */
data class CappedServicePlanEntryQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServicePlanEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedServicePlanEntry>): MutableList<CappedServicePlanEntry> {
        val matched = filter(items) { candidate: CappedServicePlanEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServicePlanEntries(store: CappedServicePlanEntryStore, query: CappedServicePlanEntryQuery): MutableList<CappedServicePlanEntry> {
    return query.apply(store.list())
}
