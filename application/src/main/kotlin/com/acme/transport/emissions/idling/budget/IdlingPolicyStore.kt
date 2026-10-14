package com.acme.transport.emissions.idling.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling policies by code. */
interface IdlingPolicyStore {
    fun find(code: String): IdlingPolicy?
    fun save(item: IdlingPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingPolicyStore(
    var items: MutableMap<String, IdlingPolicy>,
    var order: MutableList<String>,
) : IdlingPolicyStore {
    override fun find(code: String): IdlingPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingPolicy) {
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

    override fun list(): MutableList<IdlingPolicy> {
        val out = mutableListOf<IdlingPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingPolicyStore(): IdlingPolicyStore {
    return MemoryIdlingPolicyStore(mutableMapOf<String, IdlingPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingPolicyStore(
    var inner: IdlingPolicyStore,
    var cache: TtlCache<IdlingPolicy>,
    var hits: Int,
    var misses: Int,
) : IdlingPolicyStore {
    override fun find(code: String): IdlingPolicy? {
        val hit = (cache.get(code) as IdlingPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingPolicy> = inner.list()
}

fun newCachedIdlingPolicyStore(inner: IdlingPolicyStore, ttl: Int, clock: Clock): IdlingPolicyStore {
    return CachedIdlingPolicyStore(
        inner = inner,
        cache = newTtlCache<IdlingPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling policies. Empty fields match all. */
data class IdlingPolicyQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingPolicy): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<IdlingPolicy>): MutableList<IdlingPolicy> {
        val matched = filter(items) { candidate: IdlingPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingPolicies(store: IdlingPolicyStore, query: IdlingPolicyQuery): MutableList<IdlingPolicy> {
    return query.apply(store.list())
}
