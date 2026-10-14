package com.acme.transport.customs.hold.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold policies by code. */
interface HoldPolicyStore {
    fun find(code: String): HoldPolicy?
    fun save(item: HoldPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldPolicyStore(
    var items: MutableMap<String, HoldPolicy>,
    var order: MutableList<String>,
) : HoldPolicyStore {
    override fun find(code: String): HoldPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldPolicy) {
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

    override fun list(): MutableList<HoldPolicy> {
        val out = mutableListOf<HoldPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldPolicyStore(): HoldPolicyStore {
    return MemoryHoldPolicyStore(mutableMapOf<String, HoldPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldPolicyStore(
    var inner: HoldPolicyStore,
    var cache: TtlCache<HoldPolicy>,
    var hits: Int,
    var misses: Int,
) : HoldPolicyStore {
    override fun find(code: String): HoldPolicy? {
        val hit = (cache.get(code) as HoldPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldPolicy> = inner.list()
}

fun newCachedHoldPolicyStore(inner: HoldPolicyStore, ttl: Int, clock: Clock): HoldPolicyStore {
    return CachedHoldPolicyStore(
        inner = inner,
        cache = newTtlCache<HoldPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold policies. Empty fields match all. */
data class HoldPolicyQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldPolicy>): MutableList<HoldPolicy> {
        val matched = filter(items) { candidate: HoldPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldPolicies(store: HoldPolicyStore, query: HoldPolicyQuery): MutableList<HoldPolicy> {
    return query.apply(store.list())
}
