package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling backhaul policies by code. */
interface RollingBackhaulPolicyStore {
    fun find(code: String): RollingBackhaulPolicy?
    fun save(item: RollingBackhaulPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBackhaulPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBackhaulPolicyStore(
    var items: MutableMap<String, RollingBackhaulPolicy>,
    var order: MutableList<String>,
) : RollingBackhaulPolicyStore {
    override fun find(code: String): RollingBackhaulPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBackhaulPolicy) {
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

    override fun list(): MutableList<RollingBackhaulPolicy> {
        val out = mutableListOf<RollingBackhaulPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBackhaulPolicyStore(): RollingBackhaulPolicyStore {
    return MemoryRollingBackhaulPolicyStore(
        mutableMapOf<String, RollingBackhaulPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBackhaulPolicyStore(
    var inner: RollingBackhaulPolicyStore,
    var cache: TtlCache<RollingBackhaulPolicy>,
    var hits: Int,
    var misses: Int,
) : RollingBackhaulPolicyStore {
    override fun find(code: String): RollingBackhaulPolicy? {
        val hit = (cache.get(code) as RollingBackhaulPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBackhaulPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBackhaulPolicy> = inner.list()
}

fun newCachedRollingBackhaulPolicyStore(inner: RollingBackhaulPolicyStore, ttl: Int, clock: Clock): RollingBackhaulPolicyStore {
    return CachedRollingBackhaulPolicyStore(
        inner = inner,
        cache = newTtlCache<RollingBackhaulPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling backhaul policies. Empty fields match all. */
data class RollingBackhaulPolicyQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBackhaulPolicy): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingBackhaulPolicy>): MutableList<RollingBackhaulPolicy> {
        val matched = filter(items) { candidate: RollingBackhaulPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBackhaulPolicies(store: RollingBackhaulPolicyStore, query: RollingBackhaulPolicyQuery): MutableList<RollingBackhaulPolicy> {
    return query.apply(store.list())
}
