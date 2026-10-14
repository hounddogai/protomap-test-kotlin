package com.acme.transport.customs.inspection.claim

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection claims by code. */
interface InspectionClaimStore {
    fun find(code: String): InspectionClaim?
    fun save(item: InspectionClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionClaimStore(
    var items: MutableMap<String, InspectionClaim>,
    var order: MutableList<String>,
) : InspectionClaimStore {
    override fun find(code: String): InspectionClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionClaim) {
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

    override fun list(): MutableList<InspectionClaim> {
        val out = mutableListOf<InspectionClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionClaimStore(): InspectionClaimStore {
    return MemoryInspectionClaimStore(mutableMapOf<String, InspectionClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionClaimStore(
    var inner: InspectionClaimStore,
    var cache: TtlCache<InspectionClaim>,
    var hits: Int,
    var misses: Int,
) : InspectionClaimStore {
    override fun find(code: String): InspectionClaim? {
        val hit = (cache.get(code) as InspectionClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionClaim> = inner.list()
}

fun newCachedInspectionClaimStore(inner: InspectionClaimStore, ttl: Int, clock: Clock): InspectionClaimStore {
    return CachedInspectionClaimStore(
        inner = inner,
        cache = newTtlCache<InspectionClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection claims. Empty fields match all. */
data class InspectionClaimQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionClaim>): MutableList<InspectionClaim> {
        val matched = filter(items) { candidate: InspectionClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionClaims(store: InspectionClaimStore, query: InspectionClaimQuery): MutableList<InspectionClaim> {
    return query.apply(store.list())
}
