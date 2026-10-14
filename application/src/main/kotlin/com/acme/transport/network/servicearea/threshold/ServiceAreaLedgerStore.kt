package com.acme.transport.network.servicearea.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area ledgers by code. */
interface ServiceAreaLedgerStore {
    fun find(code: String): ServiceAreaLedger?
    fun save(item: ServiceAreaLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaLedgerStore(
    var items: MutableMap<String, ServiceAreaLedger>,
    var order: MutableList<String>,
) : ServiceAreaLedgerStore {
    override fun find(code: String): ServiceAreaLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaLedger) {
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

    override fun list(): MutableList<ServiceAreaLedger> {
        val out = mutableListOf<ServiceAreaLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaLedgerStore(): ServiceAreaLedgerStore {
    return MemoryServiceAreaLedgerStore(mutableMapOf<String, ServiceAreaLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaLedgerStore(
    var inner: ServiceAreaLedgerStore,
    var cache: TtlCache<ServiceAreaLedger>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaLedgerStore {
    override fun find(code: String): ServiceAreaLedger? {
        val hit = (cache.get(code) as ServiceAreaLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaLedger> = inner.list()
}

fun newCachedServiceAreaLedgerStore(inner: ServiceAreaLedgerStore, ttl: Int, clock: Clock): ServiceAreaLedgerStore {
    return CachedServiceAreaLedgerStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area ledgers. Empty fields match all. */
data class ServiceAreaLedgerQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaLedger>): MutableList<ServiceAreaLedger> {
        val matched = filter(items) { candidate: ServiceAreaLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaLedgers(store: ServiceAreaLedgerStore, query: ServiceAreaLedgerQuery): MutableList<ServiceAreaLedger> {
    return query.apply(store.list())
}
