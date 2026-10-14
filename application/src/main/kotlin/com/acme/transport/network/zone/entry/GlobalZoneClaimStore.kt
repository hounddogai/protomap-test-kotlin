package com.acme.transport.network.zone.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global zone claims by code. */
interface GlobalZoneClaimStore {
    fun find(code: String): GlobalZoneClaim?
    fun save(item: GlobalZoneClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalZoneClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalZoneClaimStore(
    var items: MutableMap<String, GlobalZoneClaim>,
    var order: MutableList<String>,
) : GlobalZoneClaimStore {
    override fun find(code: String): GlobalZoneClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalZoneClaim) {
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

    override fun list(): MutableList<GlobalZoneClaim> {
        val out = mutableListOf<GlobalZoneClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalZoneClaimStore(): GlobalZoneClaimStore {
    return MemoryGlobalZoneClaimStore(mutableMapOf<String, GlobalZoneClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalZoneClaimStore(
    var inner: GlobalZoneClaimStore,
    var cache: TtlCache<GlobalZoneClaim>,
    var hits: Int,
    var misses: Int,
) : GlobalZoneClaimStore {
    override fun find(code: String): GlobalZoneClaim? {
        val hit = (cache.get(code) as GlobalZoneClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalZoneClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalZoneClaim> = inner.list()
}

fun newCachedGlobalZoneClaimStore(inner: GlobalZoneClaimStore, ttl: Int, clock: Clock): GlobalZoneClaimStore {
    return CachedGlobalZoneClaimStore(
        inner = inner,
        cache = newTtlCache<GlobalZoneClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global zone claims. Empty fields match all. */
data class GlobalZoneClaimQuery(
    var source: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalZoneClaim): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalZoneClaim>): MutableList<GlobalZoneClaim> {
        val matched = filter(items) { candidate: GlobalZoneClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalZoneClaims(store: GlobalZoneClaimStore, query: GlobalZoneClaimQuery): MutableList<GlobalZoneClaim> {
    return query.apply(store.list())
}
