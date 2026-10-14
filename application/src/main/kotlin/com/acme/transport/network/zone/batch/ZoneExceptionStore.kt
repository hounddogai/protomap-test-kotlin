package com.acme.transport.network.zone.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone exceptions by code. */
interface ZoneExceptionStore {
    fun find(code: String): ZoneException?
    fun save(item: ZoneException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneExceptionStore(
    var items: MutableMap<String, ZoneException>,
    var order: MutableList<String>,
) : ZoneExceptionStore {
    override fun find(code: String): ZoneException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneException) {
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

    override fun list(): MutableList<ZoneException> {
        val out = mutableListOf<ZoneException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneExceptionStore(): ZoneExceptionStore {
    return MemoryZoneExceptionStore(mutableMapOf<String, ZoneException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneExceptionStore(
    var inner: ZoneExceptionStore,
    var cache: TtlCache<ZoneException>,
    var hits: Int,
    var misses: Int,
) : ZoneExceptionStore {
    override fun find(code: String): ZoneException? {
        val hit = (cache.get(code) as ZoneException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneException> = inner.list()
}

fun newCachedZoneExceptionStore(inner: ZoneExceptionStore, ttl: Int, clock: Clock): ZoneExceptionStore {
    return CachedZoneExceptionStore(
        inner = inner,
        cache = newTtlCache<ZoneException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone exceptions. Empty fields match all. */
data class ZoneExceptionQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneException): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneException>): MutableList<ZoneException> {
        val matched = filter(items) { candidate: ZoneException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneExceptions(store: ZoneExceptionStore, query: ZoneExceptionQuery): MutableList<ZoneException> {
    return query.apply(store.list())
}
