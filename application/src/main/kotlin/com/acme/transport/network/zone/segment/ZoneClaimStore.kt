package com.acme.transport.network.zone.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone claims by code. */
interface ZoneClaimStore {
    fun find(code: String): ZoneClaim?
    fun save(item: ZoneClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneClaimStore(
    var items: MutableMap<String, ZoneClaim>,
    var order: MutableList<String>,
) : ZoneClaimStore {
    override fun find(code: String): ZoneClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneClaim) {
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

    override fun list(): MutableList<ZoneClaim> {
        val out = mutableListOf<ZoneClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneClaimStore(): ZoneClaimStore {
    return MemoryZoneClaimStore(mutableMapOf<String, ZoneClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneClaimStore(
    var inner: ZoneClaimStore,
    var cache: TtlCache<ZoneClaim>,
    var hits: Int,
    var misses: Int,
) : ZoneClaimStore {
    override fun find(code: String): ZoneClaim? {
        val hit = (cache.get(code) as ZoneClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneClaim> = inner.list()
}

fun newCachedZoneClaimStore(inner: ZoneClaimStore, ttl: Int, clock: Clock): ZoneClaimStore {
    return CachedZoneClaimStore(
        inner = inner,
        cache = newTtlCache<ZoneClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone claims. Empty fields match all. */
data class ZoneClaimQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneClaim>): MutableList<ZoneClaim> {
        val matched = filter(items) { candidate: ZoneClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneClaims(store: ZoneClaimStore, query: ZoneClaimQuery): MutableList<ZoneClaim> {
    return query.apply(store.list())
}
