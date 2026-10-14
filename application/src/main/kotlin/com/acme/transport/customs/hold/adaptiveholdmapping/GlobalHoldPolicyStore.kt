package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global hold policies by code. */
interface GlobalHoldPolicyStore {
    fun find(code: String): GlobalHoldPolicy?
    fun save(item: GlobalHoldPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalHoldPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalHoldPolicyStore(
    var items: MutableMap<String, GlobalHoldPolicy>,
    var order: MutableList<String>,
) : GlobalHoldPolicyStore {
    override fun find(code: String): GlobalHoldPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalHoldPolicy) {
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

    override fun list(): MutableList<GlobalHoldPolicy> {
        val out = mutableListOf<GlobalHoldPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalHoldPolicyStore(): GlobalHoldPolicyStore {
    return MemoryGlobalHoldPolicyStore(mutableMapOf<String, GlobalHoldPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalHoldPolicyStore(
    var inner: GlobalHoldPolicyStore,
    var cache: TtlCache<GlobalHoldPolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalHoldPolicyStore {
    override fun find(code: String): GlobalHoldPolicy? {
        val hit = (cache.get(code) as GlobalHoldPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalHoldPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalHoldPolicy> = inner.list()
}

fun newCachedGlobalHoldPolicyStore(inner: GlobalHoldPolicyStore, ttl: Int, clock: Clock): GlobalHoldPolicyStore {
    return CachedGlobalHoldPolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalHoldPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global hold policies. Empty fields match all. */
data class GlobalHoldPolicyQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalHoldPolicy): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalHoldPolicy>): MutableList<GlobalHoldPolicy> {
        val matched = filter(items) { candidate: GlobalHoldPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalHoldPolicies(store: GlobalHoldPolicyStore, query: GlobalHoldPolicyQuery): MutableList<GlobalHoldPolicy> {
    return query.apply(store.list())
}
