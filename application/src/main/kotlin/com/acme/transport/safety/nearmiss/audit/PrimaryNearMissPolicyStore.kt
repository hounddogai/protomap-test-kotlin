package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary near miss policies by code. */
interface PrimaryNearMissPolicyStore {
    fun find(code: String): PrimaryNearMissPolicy?
    fun save(item: PrimaryNearMissPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNearMissPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNearMissPolicyStore(
    var items: MutableMap<String, PrimaryNearMissPolicy>,
    var order: MutableList<String>,
) : PrimaryNearMissPolicyStore {
    override fun find(code: String): PrimaryNearMissPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNearMissPolicy) {
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

    override fun list(): MutableList<PrimaryNearMissPolicy> {
        val out = mutableListOf<PrimaryNearMissPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNearMissPolicyStore(): PrimaryNearMissPolicyStore {
    return MemoryPrimaryNearMissPolicyStore(
        mutableMapOf<String, PrimaryNearMissPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNearMissPolicyStore(
    var inner: PrimaryNearMissPolicyStore,
    var cache: TtlCache<PrimaryNearMissPolicy>,
    var hits: Int,
    var misses: Int,
) : PrimaryNearMissPolicyStore {
    override fun find(code: String): PrimaryNearMissPolicy? {
        val hit = (cache.get(code) as PrimaryNearMissPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNearMissPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNearMissPolicy> = inner.list()
}

fun newCachedPrimaryNearMissPolicyStore(inner: PrimaryNearMissPolicyStore, ttl: Int, clock: Clock): PrimaryNearMissPolicyStore {
    return CachedPrimaryNearMissPolicyStore(
        inner = inner,
        cache = newTtlCache<PrimaryNearMissPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary near miss policies. Empty fields match all. */
data class PrimaryNearMissPolicyQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNearMissPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryNearMissPolicy>): MutableList<PrimaryNearMissPolicy> {
        val matched = filter(items) { candidate: PrimaryNearMissPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNearMissPolicies(store: PrimaryNearMissPolicyStore, query: PrimaryNearMissPolicyQuery): MutableList<PrimaryNearMissPolicy> {
    return query.apply(store.list())
}
