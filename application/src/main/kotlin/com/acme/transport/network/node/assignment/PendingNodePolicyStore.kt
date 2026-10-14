package com.acme.transport.network.node.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending node policies by code. */
interface PendingNodePolicyStore {
    fun find(code: String): PendingNodePolicy?
    fun save(item: PendingNodePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNodePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNodePolicyStore(
    var items: MutableMap<String, PendingNodePolicy>,
    var order: MutableList<String>,
) : PendingNodePolicyStore {
    override fun find(code: String): PendingNodePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNodePolicy) {
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

    override fun list(): MutableList<PendingNodePolicy> {
        val out = mutableListOf<PendingNodePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNodePolicyStore(): PendingNodePolicyStore {
    return MemoryPendingNodePolicyStore(mutableMapOf<String, PendingNodePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNodePolicyStore(
    var inner: PendingNodePolicyStore,
    var cache: TtlCache<PendingNodePolicy>,
    var hits: Int,
    var misses: Int,
) : PendingNodePolicyStore {
    override fun find(code: String): PendingNodePolicy? {
        val hit = (cache.get(code) as PendingNodePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNodePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNodePolicy> = inner.list()
}

fun newCachedPendingNodePolicyStore(inner: PendingNodePolicyStore, ttl: Int, clock: Clock): PendingNodePolicyStore {
    return CachedPendingNodePolicyStore(
        inner = inner,
        cache = newTtlCache<PendingNodePolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending node policies. Empty fields match all. */
data class PendingNodePolicyQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNodePolicy): Boolean {
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

    fun apply(items: MutableList<PendingNodePolicy>): MutableList<PendingNodePolicy> {
        val matched = filter(items) { candidate: PendingNodePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNodePolicies(store: PendingNodePolicyStore, query: PendingNodePolicyQuery): MutableList<PendingNodePolicy> {
    return query.apply(store.list())
}
