package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation policies by code. */
interface SortationPolicyStore {
    fun find(code: String): SortationPolicy?
    fun save(item: SortationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationPolicyStore(
    var items: MutableMap<String, SortationPolicy>,
    var order: MutableList<String>,
) : SortationPolicyStore {
    override fun find(code: String): SortationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationPolicy) {
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

    override fun list(): MutableList<SortationPolicy> {
        val out = mutableListOf<SortationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationPolicyStore(): SortationPolicyStore {
    return MemorySortationPolicyStore(mutableMapOf<String, SortationPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationPolicyStore(
    var inner: SortationPolicyStore,
    var cache: TtlCache<SortationPolicy>,
    var hits: Int,
    var misses: Int,
) : SortationPolicyStore {
    override fun find(code: String): SortationPolicy? {
        val hit = (cache.get(code) as SortationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationPolicy> = inner.list()
}

fun newCachedSortationPolicyStore(inner: SortationPolicyStore, ttl: Int, clock: Clock): SortationPolicyStore {
    return CachedSortationPolicyStore(
        inner = inner,
        cache = newTtlCache<SortationPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation policies. Empty fields match all. */
data class SortationPolicyQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationPolicy>): MutableList<SortationPolicy> {
        val matched = filter(items) { candidate: SortationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationPolicies(store: SortationPolicyStore, query: SortationPolicyQuery): MutableList<SortationPolicy> {
    return query.apply(store.list())
}
