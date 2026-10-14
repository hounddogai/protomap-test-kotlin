package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop policies by code. */
interface DropPolicyStore {
    fun find(code: String): DropPolicy?
    fun save(item: DropPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropPolicyStore(
    var items: MutableMap<String, DropPolicy>,
    var order: MutableList<String>,
) : DropPolicyStore {
    override fun find(code: String): DropPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropPolicy) {
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

    override fun list(): MutableList<DropPolicy> {
        val out = mutableListOf<DropPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropPolicyStore(): DropPolicyStore {
    return MemoryDropPolicyStore(mutableMapOf<String, DropPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropPolicyStore(
    var inner: DropPolicyStore,
    var cache: TtlCache<DropPolicy>,
    var hits: Int,
    var misses: Int,
) : DropPolicyStore {
    override fun find(code: String): DropPolicy? {
        val hit = (cache.get(code) as DropPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropPolicy> = inner.list()
}

fun newCachedDropPolicyStore(inner: DropPolicyStore, ttl: Int, clock: Clock): DropPolicyStore {
    return CachedDropPolicyStore(
        inner = inner,
        cache = newTtlCache<DropPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop policies. Empty fields match all. */
data class DropPolicyQuery(
    var source: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<DropPolicy>): MutableList<DropPolicy> {
        val matched = filter(items) { candidate: DropPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropPolicies(store: DropPolicyStore, query: DropPolicyQuery): MutableList<DropPolicy> {
    return query.apply(store.list())
}
