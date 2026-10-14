package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global utilization exceptions by code. */
interface GlobalUtilizationExceptionStore {
    fun find(code: String): GlobalUtilizationException?
    fun save(item: GlobalUtilizationException)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalUtilizationException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalUtilizationExceptionStore(
    var items: MutableMap<String, GlobalUtilizationException>,
    var order: MutableList<String>,
) : GlobalUtilizationExceptionStore {
    override fun find(code: String): GlobalUtilizationException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalUtilizationException) {
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

    override fun list(): MutableList<GlobalUtilizationException> {
        val out = mutableListOf<GlobalUtilizationException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalUtilizationExceptionStore(): GlobalUtilizationExceptionStore {
    return MemoryGlobalUtilizationExceptionStore(
        mutableMapOf<String, GlobalUtilizationException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalUtilizationExceptionStore(
    var inner: GlobalUtilizationExceptionStore,
    var cache: TtlCache<GlobalUtilizationException>,
    var hits: Int,
    var misses: Int,
) : GlobalUtilizationExceptionStore {
    override fun find(code: String): GlobalUtilizationException? {
        val hit = (cache.get(code) as GlobalUtilizationException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalUtilizationException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalUtilizationException> = inner.list()
}

fun newCachedGlobalUtilizationExceptionStore(inner: GlobalUtilizationExceptionStore, ttl: Int, clock: Clock): GlobalUtilizationExceptionStore {
    return CachedGlobalUtilizationExceptionStore(
        inner = inner,
        cache = newTtlCache<GlobalUtilizationException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global utilization exceptions. Empty fields match all. */
data class GlobalUtilizationExceptionQuery(
    var channel: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalUtilizationException): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<GlobalUtilizationException>): MutableList<GlobalUtilizationException> {
        val matched = filter(items) { candidate: GlobalUtilizationException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalUtilizationExceptions(store: GlobalUtilizationExceptionStore, query: GlobalUtilizationExceptionQuery): MutableList<GlobalUtilizationException> {
    return query.apply(store.list())
}
