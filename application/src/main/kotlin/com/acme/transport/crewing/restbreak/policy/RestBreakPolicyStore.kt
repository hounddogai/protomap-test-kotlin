package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break policies by code. */
interface RestBreakPolicyStore {
    fun find(code: String): RestBreakPolicy?
    fun save(item: RestBreakPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakPolicyStore(
    var items: MutableMap<String, RestBreakPolicy>,
    var order: MutableList<String>,
) : RestBreakPolicyStore {
    override fun find(code: String): RestBreakPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakPolicy) {
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

    override fun list(): MutableList<RestBreakPolicy> {
        val out = mutableListOf<RestBreakPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakPolicyStore(): RestBreakPolicyStore {
    return MemoryRestBreakPolicyStore(mutableMapOf<String, RestBreakPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakPolicyStore(
    var inner: RestBreakPolicyStore,
    var cache: TtlCache<RestBreakPolicy>,
    var hits: Int,
    var misses: Int,
) : RestBreakPolicyStore {
    override fun find(code: String): RestBreakPolicy? {
        val hit = (cache.get(code) as RestBreakPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakPolicy> = inner.list()
}

fun newCachedRestBreakPolicyStore(inner: RestBreakPolicyStore, ttl: Int, clock: Clock): RestBreakPolicyStore {
    return CachedRestBreakPolicyStore(
        inner = inner,
        cache = newTtlCache<RestBreakPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break policies. Empty fields match all. */
data class RestBreakPolicyQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakPolicy): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakPolicy>): MutableList<RestBreakPolicy> {
        val matched = filter(items) { candidate: RestBreakPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakPolicies(store: RestBreakPolicyStore, query: RestBreakPolicyQuery): MutableList<RestBreakPolicy> {
    return query.apply(store.list())
}
