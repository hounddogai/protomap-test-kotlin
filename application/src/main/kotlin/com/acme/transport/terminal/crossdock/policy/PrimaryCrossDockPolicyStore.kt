package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary cross dock policies by code. */
interface PrimaryCrossDockPolicyStore {
    fun find(code: String): PrimaryCrossDockPolicy?
    fun save(item: PrimaryCrossDockPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCrossDockPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCrossDockPolicyStore(
    var items: MutableMap<String, PrimaryCrossDockPolicy>,
    var order: MutableList<String>,
) : PrimaryCrossDockPolicyStore {
    override fun find(code: String): PrimaryCrossDockPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCrossDockPolicy) {
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

    override fun list(): MutableList<PrimaryCrossDockPolicy> {
        val out = mutableListOf<PrimaryCrossDockPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCrossDockPolicyStore(): PrimaryCrossDockPolicyStore {
    return MemoryPrimaryCrossDockPolicyStore(
        mutableMapOf<String, PrimaryCrossDockPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCrossDockPolicyStore(
    var inner: PrimaryCrossDockPolicyStore,
    var cache: TtlCache<PrimaryCrossDockPolicy>,
    var hits: Int,
    var misses: Int,
) : PrimaryCrossDockPolicyStore {
    override fun find(code: String): PrimaryCrossDockPolicy? {
        val hit = (cache.get(code) as PrimaryCrossDockPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCrossDockPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCrossDockPolicy> = inner.list()
}

fun newCachedPrimaryCrossDockPolicyStore(inner: PrimaryCrossDockPolicyStore, ttl: Int, clock: Clock): PrimaryCrossDockPolicyStore {
    return CachedPrimaryCrossDockPolicyStore(
        inner = inner,
        cache = newTtlCache<PrimaryCrossDockPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary cross dock policies. Empty fields match all. */
data class PrimaryCrossDockPolicyQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCrossDockPolicy): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCrossDockPolicy>): MutableList<PrimaryCrossDockPolicy> {
        val matched = filter(items) { candidate: PrimaryCrossDockPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCrossDockPolicies(store: PrimaryCrossDockPolicyStore, query: PrimaryCrossDockPolicyQuery): MutableList<PrimaryCrossDockPolicy> {
    return query.apply(store.list())
}
