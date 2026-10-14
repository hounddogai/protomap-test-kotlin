package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer policies by code. */
interface TrailerPolicyStore {
    fun find(code: String): TrailerPolicy?
    fun save(item: TrailerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerPolicyStore(
    var items: MutableMap<String, TrailerPolicy>,
    var order: MutableList<String>,
) : TrailerPolicyStore {
    override fun find(code: String): TrailerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerPolicy) {
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

    override fun list(): MutableList<TrailerPolicy> {
        val out = mutableListOf<TrailerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerPolicyStore(): TrailerPolicyStore {
    return MemoryTrailerPolicyStore(mutableMapOf<String, TrailerPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerPolicyStore(
    var inner: TrailerPolicyStore,
    var cache: TtlCache<TrailerPolicy>,
    var hits: Int,
    var misses: Int,
) : TrailerPolicyStore {
    override fun find(code: String): TrailerPolicy? {
        val hit = (cache.get(code) as TrailerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerPolicy> = inner.list()
}

fun newCachedTrailerPolicyStore(inner: TrailerPolicyStore, ttl: Int, clock: Clock): TrailerPolicyStore {
    return CachedTrailerPolicyStore(
        inner = inner,
        cache = newTtlCache<TrailerPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer policies. Empty fields match all. */
data class TrailerPolicyQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TrailerPolicy>): MutableList<TrailerPolicy> {
        val matched = filter(items) { candidate: TrailerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerPolicies(store: TrailerPolicyStore, query: TrailerPolicyQuery): MutableList<TrailerPolicy> {
    return query.apply(store.list())
}
