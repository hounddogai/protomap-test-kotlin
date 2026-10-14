package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy redelivery policies by code. */
interface LegacyRedeliveryPolicyStore {
    fun find(code: String): LegacyRedeliveryPolicy?
    fun save(item: LegacyRedeliveryPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRedeliveryPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRedeliveryPolicyStore(
    var items: MutableMap<String, LegacyRedeliveryPolicy>,
    var order: MutableList<String>,
) : LegacyRedeliveryPolicyStore {
    override fun find(code: String): LegacyRedeliveryPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRedeliveryPolicy) {
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

    override fun list(): MutableList<LegacyRedeliveryPolicy> {
        val out = mutableListOf<LegacyRedeliveryPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRedeliveryPolicyStore(): LegacyRedeliveryPolicyStore {
    return MemoryLegacyRedeliveryPolicyStore(
        mutableMapOf<String, LegacyRedeliveryPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRedeliveryPolicyStore(
    var inner: LegacyRedeliveryPolicyStore,
    var cache: TtlCache<LegacyRedeliveryPolicy>,
    var hits: Int,
    var misses: Int,
) : LegacyRedeliveryPolicyStore {
    override fun find(code: String): LegacyRedeliveryPolicy? {
        val hit = (cache.get(code) as LegacyRedeliveryPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRedeliveryPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRedeliveryPolicy> = inner.list()
}

fun newCachedLegacyRedeliveryPolicyStore(inner: LegacyRedeliveryPolicyStore, ttl: Int, clock: Clock): LegacyRedeliveryPolicyStore {
    return CachedLegacyRedeliveryPolicyStore(
        inner = inner,
        cache = newTtlCache<LegacyRedeliveryPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy redelivery policies. Empty fields match all. */
data class LegacyRedeliveryPolicyQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRedeliveryPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRedeliveryPolicy>): MutableList<LegacyRedeliveryPolicy> {
        val matched = filter(items) { candidate: LegacyRedeliveryPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRedeliveryPolicies(store: LegacyRedeliveryPolicyStore, query: LegacyRedeliveryPolicyQuery): MutableList<LegacyRedeliveryPolicy> {
    return query.apply(store.list())
}
