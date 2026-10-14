package com.acme.transport.network.hub.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global hub policies by code. */
interface GlobalHubPolicyStore {
    fun find(code: String): GlobalHubPolicy?
    fun save(item: GlobalHubPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalHubPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalHubPolicyStore(
    var items: MutableMap<String, GlobalHubPolicy>,
    var order: MutableList<String>,
) : GlobalHubPolicyStore {
    override fun find(code: String): GlobalHubPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalHubPolicy) {
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

    override fun list(): MutableList<GlobalHubPolicy> {
        val out = mutableListOf<GlobalHubPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalHubPolicyStore(): GlobalHubPolicyStore {
    return MemoryGlobalHubPolicyStore(mutableMapOf<String, GlobalHubPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalHubPolicyStore(
    var inner: GlobalHubPolicyStore,
    var cache: TtlCache<GlobalHubPolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalHubPolicyStore {
    override fun find(code: String): GlobalHubPolicy? {
        val hit = (cache.get(code) as GlobalHubPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalHubPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalHubPolicy> = inner.list()
}

fun newCachedGlobalHubPolicyStore(inner: GlobalHubPolicyStore, ttl: Int, clock: Clock): GlobalHubPolicyStore {
    return CachedGlobalHubPolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalHubPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global hub policies. Empty fields match all. */
data class GlobalHubPolicyQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalHubPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalHubPolicy>): MutableList<GlobalHubPolicy> {
        val matched = filter(items) { candidate: GlobalHubPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalHubPolicies(store: GlobalHubPolicyStore, query: GlobalHubPolicyQuery): MutableList<GlobalHubPolicy> {
    return query.apply(store.list())
}
