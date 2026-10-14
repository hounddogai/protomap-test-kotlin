package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area entries by code. */
interface ServiceAreaEntryStore {
    fun find(code: String): ServiceAreaEntry?
    fun save(item: ServiceAreaEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaEntryStore(
    var items: MutableMap<String, ServiceAreaEntry>,
    var order: MutableList<String>,
) : ServiceAreaEntryStore {
    override fun find(code: String): ServiceAreaEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaEntry) {
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

    override fun list(): MutableList<ServiceAreaEntry> {
        val out = mutableListOf<ServiceAreaEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaEntryStore(): ServiceAreaEntryStore {
    return MemoryServiceAreaEntryStore(mutableMapOf<String, ServiceAreaEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaEntryStore(
    var inner: ServiceAreaEntryStore,
    var cache: TtlCache<ServiceAreaEntry>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaEntryStore {
    override fun find(code: String): ServiceAreaEntry? {
        val hit = (cache.get(code) as ServiceAreaEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaEntry> = inner.list()
}

fun newCachedServiceAreaEntryStore(inner: ServiceAreaEntryStore, ttl: Int, clock: Clock): ServiceAreaEntryStore {
    return CachedServiceAreaEntryStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area entries. Empty fields match all. */
data class ServiceAreaEntryQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaEntry>): MutableList<ServiceAreaEntry> {
        val matched = filter(items) { candidate: ServiceAreaEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaEntries(store: ServiceAreaEntryStore, query: ServiceAreaEntryQuery): MutableList<ServiceAreaEntry> {
    return query.apply(store.list())
}
