package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery policies by code. */
interface RedeliveryPolicyStore {
    fun find(code: String): RedeliveryPolicy?
    fun save(item: RedeliveryPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryPolicyStore(
    var items: MutableMap<String, RedeliveryPolicy>,
    var order: MutableList<String>,
) : RedeliveryPolicyStore {
    override fun find(code: String): RedeliveryPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryPolicy) {
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

    override fun list(): MutableList<RedeliveryPolicy> {
        val out = mutableListOf<RedeliveryPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryPolicyStore(): RedeliveryPolicyStore {
    return MemoryRedeliveryPolicyStore(mutableMapOf<String, RedeliveryPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryPolicyStore(
    var inner: RedeliveryPolicyStore,
    var cache: TtlCache<RedeliveryPolicy>,
    var hits: Int,
    var misses: Int,
) : RedeliveryPolicyStore {
    override fun find(code: String): RedeliveryPolicy? {
        val hit = (cache.get(code) as RedeliveryPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryPolicy> = inner.list()
}

fun newCachedRedeliveryPolicyStore(inner: RedeliveryPolicyStore, ttl: Int, clock: Clock): RedeliveryPolicyStore {
    return CachedRedeliveryPolicyStore(
        inner = inner,
        cache = newTtlCache<RedeliveryPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery policies. Empty fields match all. */
data class RedeliveryPolicyQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryPolicy>): MutableList<RedeliveryPolicy> {
        val matched = filter(items) { candidate: RedeliveryPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryPolicies(store: RedeliveryPolicyStore, query: RedeliveryPolicyQuery): MutableList<RedeliveryPolicy> {
    return query.apply(store.list())
}
