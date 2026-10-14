package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global node policies by code. */
interface GlobalNodePolicyStore {
    fun find(code: String): GlobalNodePolicy?
    fun save(item: GlobalNodePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalNodePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalNodePolicyStore(
    var items: MutableMap<String, GlobalNodePolicy>,
    var order: MutableList<String>,
) : GlobalNodePolicyStore {
    override fun find(code: String): GlobalNodePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalNodePolicy) {
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

    override fun list(): MutableList<GlobalNodePolicy> {
        val out = mutableListOf<GlobalNodePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalNodePolicyStore(): GlobalNodePolicyStore {
    return MemoryGlobalNodePolicyStore(mutableMapOf<String, GlobalNodePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalNodePolicyStore(
    var inner: GlobalNodePolicyStore,
    var cache: TtlCache<GlobalNodePolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalNodePolicyStore {
    override fun find(code: String): GlobalNodePolicy? {
        val hit = (cache.get(code) as GlobalNodePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalNodePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalNodePolicy> = inner.list()
}

fun newCachedGlobalNodePolicyStore(inner: GlobalNodePolicyStore, ttl: Int, clock: Clock): GlobalNodePolicyStore {
    return CachedGlobalNodePolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalNodePolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global node policies. Empty fields match all. */
data class GlobalNodePolicyQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalNodePolicy): Boolean {
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

    fun apply(items: MutableList<GlobalNodePolicy>): MutableList<GlobalNodePolicy> {
        val matched = filter(items) { candidate: GlobalNodePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalNodePolicies(store: GlobalNodePolicyStore, query: GlobalNodePolicyQuery): MutableList<GlobalNodePolicy> {
    return query.apply(store.list())
}
