package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect claims by code. */
interface DefectClaimStore {
    fun find(code: String): DefectClaim?
    fun save(item: DefectClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectClaimStore(
    var items: MutableMap<String, DefectClaim>,
    var order: MutableList<String>,
) : DefectClaimStore {
    override fun find(code: String): DefectClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectClaim) {
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

    override fun list(): MutableList<DefectClaim> {
        val out = mutableListOf<DefectClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectClaimStore(): DefectClaimStore {
    return MemoryDefectClaimStore(mutableMapOf<String, DefectClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectClaimStore(
    var inner: DefectClaimStore,
    var cache: TtlCache<DefectClaim>,
    var hits: Int,
    var misses: Int,
) : DefectClaimStore {
    override fun find(code: String): DefectClaim? {
        val hit = (cache.get(code) as DefectClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectClaim> = inner.list()
}

fun newCachedDefectClaimStore(inner: DefectClaimStore, ttl: Int, clock: Clock): DefectClaimStore {
    return CachedDefectClaimStore(
        inner = inner,
        cache = newTtlCache<DefectClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect claims. Empty fields match all. */
data class DefectClaimQuery(
    var locale: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectClaim): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectClaim>): MutableList<DefectClaim> {
        val matched = filter(items) { candidate: DefectClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectClaims(store: DefectClaimStore, query: DefectClaimQuery): MutableList<DefectClaim> {
    return query.apply(store.list())
}
