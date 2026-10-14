package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global trailer yard policies by code. */
interface GlobalTrailerYardPolicyStore {
    fun find(code: String): GlobalTrailerYardPolicy?
    fun save(item: GlobalTrailerYardPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerYardPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerYardPolicyStore(
    var items: MutableMap<String, GlobalTrailerYardPolicy>,
    var order: MutableList<String>,
) : GlobalTrailerYardPolicyStore {
    override fun find(code: String): GlobalTrailerYardPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerYardPolicy) {
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

    override fun list(): MutableList<GlobalTrailerYardPolicy> {
        val out = mutableListOf<GlobalTrailerYardPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerYardPolicyStore(): GlobalTrailerYardPolicyStore {
    return MemoryGlobalTrailerYardPolicyStore(
        mutableMapOf<String, GlobalTrailerYardPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerYardPolicyStore(
    var inner: GlobalTrailerYardPolicyStore,
    var cache: TtlCache<GlobalTrailerYardPolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerYardPolicyStore {
    override fun find(code: String): GlobalTrailerYardPolicy? {
        val hit = (cache.get(code) as GlobalTrailerYardPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerYardPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerYardPolicy> = inner.list()
}

fun newCachedGlobalTrailerYardPolicyStore(inner: GlobalTrailerYardPolicyStore, ttl: Int, clock: Clock): GlobalTrailerYardPolicyStore {
    return CachedGlobalTrailerYardPolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerYardPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer yard policies. Empty fields match all. */
data class GlobalTrailerYardPolicyQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerYardPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTrailerYardPolicy>): MutableList<GlobalTrailerYardPolicy> {
        val matched = filter(items) { candidate: GlobalTrailerYardPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerYardPolicies(store: GlobalTrailerYardPolicyStore, query: GlobalTrailerYardPolicyQuery): MutableList<GlobalTrailerYardPolicy> {
    return query.apply(store.list())
}
