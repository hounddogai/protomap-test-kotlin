package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan policies by code. */
interface LanePlanPolicyStore {
    fun find(code: String): LanePlanPolicy?
    fun save(item: LanePlanPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanPolicyStore(
    var items: MutableMap<String, LanePlanPolicy>,
    var order: MutableList<String>,
) : LanePlanPolicyStore {
    override fun find(code: String): LanePlanPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanPolicy) {
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

    override fun list(): MutableList<LanePlanPolicy> {
        val out = mutableListOf<LanePlanPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanPolicyStore(): LanePlanPolicyStore {
    return MemoryLanePlanPolicyStore(mutableMapOf<String, LanePlanPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanPolicyStore(
    var inner: LanePlanPolicyStore,
    var cache: TtlCache<LanePlanPolicy>,
    var hits: Int,
    var misses: Int,
) : LanePlanPolicyStore {
    override fun find(code: String): LanePlanPolicy? {
        val hit = (cache.get(code) as LanePlanPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanPolicy> = inner.list()
}

fun newCachedLanePlanPolicyStore(inner: LanePlanPolicyStore, ttl: Int, clock: Clock): LanePlanPolicyStore {
    return CachedLanePlanPolicyStore(
        inner = inner,
        cache = newTtlCache<LanePlanPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan policies. Empty fields match all. */
data class LanePlanPolicyQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LanePlanPolicy>): MutableList<LanePlanPolicy> {
        val matched = filter(items) { candidate: LanePlanPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanPolicies(store: LanePlanPolicyStore, query: LanePlanPolicyQuery): MutableList<LanePlanPolicy> {
    return query.apply(store.list())
}
