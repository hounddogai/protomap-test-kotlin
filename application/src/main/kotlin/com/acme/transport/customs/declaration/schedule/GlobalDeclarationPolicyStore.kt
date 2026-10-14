package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global declaration policies by code. */
interface GlobalDeclarationPolicyStore {
    fun find(code: String): GlobalDeclarationPolicy?
    fun save(item: GlobalDeclarationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDeclarationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDeclarationPolicyStore(
    var items: MutableMap<String, GlobalDeclarationPolicy>,
    var order: MutableList<String>,
) : GlobalDeclarationPolicyStore {
    override fun find(code: String): GlobalDeclarationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDeclarationPolicy) {
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

    override fun list(): MutableList<GlobalDeclarationPolicy> {
        val out = mutableListOf<GlobalDeclarationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDeclarationPolicyStore(): GlobalDeclarationPolicyStore {
    return MemoryGlobalDeclarationPolicyStore(
        mutableMapOf<String, GlobalDeclarationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDeclarationPolicyStore(
    var inner: GlobalDeclarationPolicyStore,
    var cache: TtlCache<GlobalDeclarationPolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalDeclarationPolicyStore {
    override fun find(code: String): GlobalDeclarationPolicy? {
        val hit = (cache.get(code) as GlobalDeclarationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDeclarationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDeclarationPolicy> = inner.list()
}

fun newCachedGlobalDeclarationPolicyStore(inner: GlobalDeclarationPolicyStore, ttl: Int, clock: Clock): GlobalDeclarationPolicyStore {
    return CachedGlobalDeclarationPolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalDeclarationPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global declaration policies. Empty fields match all. */
data class GlobalDeclarationPolicyQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDeclarationPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDeclarationPolicy>): MutableList<GlobalDeclarationPolicy> {
        val matched = filter(items) { candidate: GlobalDeclarationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDeclarationPolicies(store: GlobalDeclarationPolicyStore, query: GlobalDeclarationPolicyQuery): MutableList<GlobalDeclarationPolicy> {
    return query.apply(store.list())
}
