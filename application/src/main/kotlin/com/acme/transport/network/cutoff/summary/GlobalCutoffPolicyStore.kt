package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global cutoff policies by code. */
interface GlobalCutoffPolicyStore {
    fun find(code: String): GlobalCutoffPolicy?
    fun save(item: GlobalCutoffPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCutoffPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCutoffPolicyStore(
    var items: MutableMap<String, GlobalCutoffPolicy>,
    var order: MutableList<String>,
) : GlobalCutoffPolicyStore {
    override fun find(code: String): GlobalCutoffPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCutoffPolicy) {
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

    override fun list(): MutableList<GlobalCutoffPolicy> {
        val out = mutableListOf<GlobalCutoffPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCutoffPolicyStore(): GlobalCutoffPolicyStore {
    return MemoryGlobalCutoffPolicyStore(mutableMapOf<String, GlobalCutoffPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCutoffPolicyStore(
    var inner: GlobalCutoffPolicyStore,
    var cache: TtlCache<GlobalCutoffPolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalCutoffPolicyStore {
    override fun find(code: String): GlobalCutoffPolicy? {
        val hit = (cache.get(code) as GlobalCutoffPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCutoffPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCutoffPolicy> = inner.list()
}

fun newCachedGlobalCutoffPolicyStore(inner: GlobalCutoffPolicyStore, ttl: Int, clock: Clock): GlobalCutoffPolicyStore {
    return CachedGlobalCutoffPolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalCutoffPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cutoff policies. Empty fields match all. */
data class GlobalCutoffPolicyQuery(
    var segment: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCutoffPolicy): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<GlobalCutoffPolicy>): MutableList<GlobalCutoffPolicy> {
        val matched = filter(items) { candidate: GlobalCutoffPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCutoffPolicies(store: GlobalCutoffPolicyStore, query: GlobalCutoffPolicyQuery): MutableList<GlobalCutoffPolicy> {
    return query.apply(store.list())
}
