package com.acme.transport.customs.clearance.target

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance claims by code. */
interface ClearanceClaimStore {
    fun find(code: String): ClearanceClaim?
    fun save(item: ClearanceClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceClaimStore(
    var items: MutableMap<String, ClearanceClaim>,
    var order: MutableList<String>,
) : ClearanceClaimStore {
    override fun find(code: String): ClearanceClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceClaim) {
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

    override fun list(): MutableList<ClearanceClaim> {
        val out = mutableListOf<ClearanceClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceClaimStore(): ClearanceClaimStore {
    return MemoryClearanceClaimStore(mutableMapOf<String, ClearanceClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceClaimStore(
    var inner: ClearanceClaimStore,
    var cache: TtlCache<ClearanceClaim>,
    var hits: Int,
    var misses: Int,
) : ClearanceClaimStore {
    override fun find(code: String): ClearanceClaim? {
        val hit = (cache.get(code) as ClearanceClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceClaim> = inner.list()
}

fun newCachedClearanceClaimStore(inner: ClearanceClaimStore, ttl: Int, clock: Clock): ClearanceClaimStore {
    return CachedClearanceClaimStore(
        inner = inner,
        cache = newTtlCache<ClearanceClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance claims. Empty fields match all. */
data class ClearanceClaimQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceClaim>): MutableList<ClearanceClaim> {
        val matched = filter(items) { candidate: ClearanceClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceClaims(store: ClearanceClaimStore, query: ClearanceClaimQuery): MutableList<ClearanceClaim> {
    return query.apply(store.list())
}
