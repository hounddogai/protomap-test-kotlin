package com.acme.transport.network.node.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual node policies by code. */
interface ManualNodePolicyStore {
    fun find(code: String): ManualNodePolicy?
    fun save(item: ManualNodePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualNodePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualNodePolicyStore(
    var items: MutableMap<String, ManualNodePolicy>,
    var order: MutableList<String>,
) : ManualNodePolicyStore {
    override fun find(code: String): ManualNodePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualNodePolicy) {
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

    override fun list(): MutableList<ManualNodePolicy> {
        val out = mutableListOf<ManualNodePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualNodePolicyStore(): ManualNodePolicyStore {
    return MemoryManualNodePolicyStore(mutableMapOf<String, ManualNodePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualNodePolicyStore(
    var inner: ManualNodePolicyStore,
    var cache: TtlCache<ManualNodePolicy>,
    var hits: Int,
    var misses: Int,
) : ManualNodePolicyStore {
    override fun find(code: String): ManualNodePolicy? {
        val hit = (cache.get(code) as ManualNodePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualNodePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualNodePolicy> = inner.list()
}

fun newCachedManualNodePolicyStore(inner: ManualNodePolicyStore, ttl: Int, clock: Clock): ManualNodePolicyStore {
    return CachedManualNodePolicyStore(
        inner = inner,
        cache = newTtlCache<ManualNodePolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual node policies. Empty fields match all. */
data class ManualNodePolicyQuery(
    var locale: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualNodePolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualNodePolicy>): MutableList<ManualNodePolicy> {
        val matched = filter(items) { candidate: ManualNodePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualNodePolicies(store: ManualNodePolicyStore, query: ManualNodePolicyQuery): MutableList<ManualNodePolicy> {
    return query.apply(store.list())
}
