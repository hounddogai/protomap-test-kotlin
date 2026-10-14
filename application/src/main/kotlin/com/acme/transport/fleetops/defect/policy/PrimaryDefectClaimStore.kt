package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary defect claims by code. */
interface PrimaryDefectClaimStore {
    fun find(code: String): PrimaryDefectClaim?
    fun save(item: PrimaryDefectClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDefectClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDefectClaimStore(
    var items: MutableMap<String, PrimaryDefectClaim>,
    var order: MutableList<String>,
) : PrimaryDefectClaimStore {
    override fun find(code: String): PrimaryDefectClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDefectClaim) {
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

    override fun list(): MutableList<PrimaryDefectClaim> {
        val out = mutableListOf<PrimaryDefectClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDefectClaimStore(): PrimaryDefectClaimStore {
    return MemoryPrimaryDefectClaimStore(mutableMapOf<String, PrimaryDefectClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDefectClaimStore(
    var inner: PrimaryDefectClaimStore,
    var cache: TtlCache<PrimaryDefectClaim>,
    var hits: Int,
    var misses: Int,
) : PrimaryDefectClaimStore {
    override fun find(code: String): PrimaryDefectClaim? {
        val hit = (cache.get(code) as PrimaryDefectClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDefectClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDefectClaim> = inner.list()
}

fun newCachedPrimaryDefectClaimStore(inner: PrimaryDefectClaimStore, ttl: Int, clock: Clock): PrimaryDefectClaimStore {
    return CachedPrimaryDefectClaimStore(
        inner = inner,
        cache = newTtlCache<PrimaryDefectClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary defect claims. Empty fields match all. */
data class PrimaryDefectClaimQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDefectClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDefectClaim>): MutableList<PrimaryDefectClaim> {
        val matched = filter(items) { candidate: PrimaryDefectClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDefectClaims(store: PrimaryDefectClaimStore, query: PrimaryDefectClaimQuery): MutableList<PrimaryDefectClaim> {
    return query.apply(store.list())
}
