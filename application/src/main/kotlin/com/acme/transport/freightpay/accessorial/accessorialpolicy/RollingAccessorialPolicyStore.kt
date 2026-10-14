package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling accessorial policies by code. */
interface RollingAccessorialPolicyStore {
    fun find(code: String): RollingAccessorialPolicy?
    fun save(item: RollingAccessorialPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAccessorialPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAccessorialPolicyStore(
    var items: MutableMap<String, RollingAccessorialPolicy>,
    var order: MutableList<String>,
) : RollingAccessorialPolicyStore {
    override fun find(code: String): RollingAccessorialPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAccessorialPolicy) {
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

    override fun list(): MutableList<RollingAccessorialPolicy> {
        val out = mutableListOf<RollingAccessorialPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAccessorialPolicyStore(): RollingAccessorialPolicyStore {
    return MemoryRollingAccessorialPolicyStore(
        mutableMapOf<String, RollingAccessorialPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAccessorialPolicyStore(
    var inner: RollingAccessorialPolicyStore,
    var cache: TtlCache<RollingAccessorialPolicy>,
    var hits: Int,
    var misses: Int,
) : RollingAccessorialPolicyStore {
    override fun find(code: String): RollingAccessorialPolicy? {
        val hit = (cache.get(code) as RollingAccessorialPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAccessorialPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAccessorialPolicy> = inner.list()
}

fun newCachedRollingAccessorialPolicyStore(inner: RollingAccessorialPolicyStore, ttl: Int, clock: Clock): RollingAccessorialPolicyStore {
    return CachedRollingAccessorialPolicyStore(
        inner = inner,
        cache = newTtlCache<RollingAccessorialPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling accessorial policies. Empty fields match all. */
data class RollingAccessorialPolicyQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAccessorialPolicy): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<RollingAccessorialPolicy>): MutableList<RollingAccessorialPolicy> {
        val matched = filter(items) { candidate: RollingAccessorialPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAccessorialPolicies(store: RollingAccessorialPolicyStore, query: RollingAccessorialPolicyQuery): MutableList<RollingAccessorialPolicy> {
    return query.apply(store.list())
}
