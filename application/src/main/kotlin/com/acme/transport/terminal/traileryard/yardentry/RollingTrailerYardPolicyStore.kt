package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling trailer yard policies by code. */
interface RollingTrailerYardPolicyStore {
    fun find(code: String): RollingTrailerYardPolicy?
    fun save(item: RollingTrailerYardPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerYardPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerYardPolicyStore(
    var items: MutableMap<String, RollingTrailerYardPolicy>,
    var order: MutableList<String>,
) : RollingTrailerYardPolicyStore {
    override fun find(code: String): RollingTrailerYardPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerYardPolicy) {
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

    override fun list(): MutableList<RollingTrailerYardPolicy> {
        val out = mutableListOf<RollingTrailerYardPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerYardPolicyStore(): RollingTrailerYardPolicyStore {
    return MemoryRollingTrailerYardPolicyStore(
        mutableMapOf<String, RollingTrailerYardPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerYardPolicyStore(
    var inner: RollingTrailerYardPolicyStore,
    var cache: TtlCache<RollingTrailerYardPolicy>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerYardPolicyStore {
    override fun find(code: String): RollingTrailerYardPolicy? {
        val hit = (cache.get(code) as RollingTrailerYardPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerYardPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerYardPolicy> = inner.list()
}

fun newCachedRollingTrailerYardPolicyStore(inner: RollingTrailerYardPolicyStore, ttl: Int, clock: Clock): RollingTrailerYardPolicyStore {
    return CachedRollingTrailerYardPolicyStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerYardPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer yard policies. Empty fields match all. */
data class RollingTrailerYardPolicyQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerYardPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTrailerYardPolicy>): MutableList<RollingTrailerYardPolicy> {
        val matched = filter(items) { candidate: RollingTrailerYardPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerYardPolicies(store: RollingTrailerYardPolicyStore, query: RollingTrailerYardPolicyQuery): MutableList<RollingTrailerYardPolicy> {
    return query.apply(store.list())
}
