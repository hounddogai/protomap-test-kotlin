package com.acme.transport.crewing.availability.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability claims by code. */
interface AvailabilityClaimStore {
    fun find(code: String): AvailabilityClaim?
    fun save(item: AvailabilityClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityClaimStore(
    var items: MutableMap<String, AvailabilityClaim>,
    var order: MutableList<String>,
) : AvailabilityClaimStore {
    override fun find(code: String): AvailabilityClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityClaim) {
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

    override fun list(): MutableList<AvailabilityClaim> {
        val out = mutableListOf<AvailabilityClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityClaimStore(): AvailabilityClaimStore {
    return MemoryAvailabilityClaimStore(mutableMapOf<String, AvailabilityClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityClaimStore(
    var inner: AvailabilityClaimStore,
    var cache: TtlCache<AvailabilityClaim>,
    var hits: Int,
    var misses: Int,
) : AvailabilityClaimStore {
    override fun find(code: String): AvailabilityClaim? {
        val hit = (cache.get(code) as AvailabilityClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityClaim> = inner.list()
}

fun newCachedAvailabilityClaimStore(inner: AvailabilityClaimStore, ttl: Int, clock: Clock): AvailabilityClaimStore {
    return CachedAvailabilityClaimStore(
        inner = inner,
        cache = newTtlCache<AvailabilityClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability claims. Empty fields match all. */
data class AvailabilityClaimQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityClaim>): MutableList<AvailabilityClaim> {
        val matched = filter(items) { candidate: AvailabilityClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityClaims(store: AvailabilityClaimStore, query: AvailabilityClaimQuery): MutableList<AvailabilityClaim> {
    return query.apply(store.list())
}
