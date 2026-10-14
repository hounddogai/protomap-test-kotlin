package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time policies by code. */
interface TransitTimePolicyStore {
    fun find(code: String): TransitTimePolicy?
    fun save(item: TransitTimePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimePolicyStore(
    var items: MutableMap<String, TransitTimePolicy>,
    var order: MutableList<String>,
) : TransitTimePolicyStore {
    override fun find(code: String): TransitTimePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimePolicy) {
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

    override fun list(): MutableList<TransitTimePolicy> {
        val out = mutableListOf<TransitTimePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimePolicyStore(): TransitTimePolicyStore {
    return MemoryTransitTimePolicyStore(mutableMapOf<String, TransitTimePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimePolicyStore(
    var inner: TransitTimePolicyStore,
    var cache: TtlCache<TransitTimePolicy>,
    var hits: Int,
    var misses: Int,
) : TransitTimePolicyStore {
    override fun find(code: String): TransitTimePolicy? {
        val hit = (cache.get(code) as TransitTimePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimePolicy> = inner.list()
}

fun newCachedTransitTimePolicyStore(inner: TransitTimePolicyStore, ttl: Int, clock: Clock): TransitTimePolicyStore {
    return CachedTransitTimePolicyStore(
        inner = inner,
        cache = newTtlCache<TransitTimePolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time policies. Empty fields match all. */
data class TransitTimePolicyQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimePolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimePolicy>): MutableList<TransitTimePolicy> {
        val matched = filter(items) { candidate: TransitTimePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimePolicies(store: TransitTimePolicyStore, query: TransitTimePolicyQuery): MutableList<TransitTimePolicy> {
    return query.apply(store.list())
}
