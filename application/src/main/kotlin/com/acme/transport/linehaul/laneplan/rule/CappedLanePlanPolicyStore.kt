package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped lane plan policies by code. */
interface CappedLanePlanPolicyStore {
    fun find(code: String): CappedLanePlanPolicy?
    fun save(item: CappedLanePlanPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLanePlanPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLanePlanPolicyStore(
    var items: MutableMap<String, CappedLanePlanPolicy>,
    var order: MutableList<String>,
) : CappedLanePlanPolicyStore {
    override fun find(code: String): CappedLanePlanPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLanePlanPolicy) {
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

    override fun list(): MutableList<CappedLanePlanPolicy> {
        val out = mutableListOf<CappedLanePlanPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLanePlanPolicyStore(): CappedLanePlanPolicyStore {
    return MemoryCappedLanePlanPolicyStore(
        mutableMapOf<String, CappedLanePlanPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLanePlanPolicyStore(
    var inner: CappedLanePlanPolicyStore,
    var cache: TtlCache<CappedLanePlanPolicy>,
    var hits: Int,
    var misses: Int,
) : CappedLanePlanPolicyStore {
    override fun find(code: String): CappedLanePlanPolicy? {
        val hit = (cache.get(code) as CappedLanePlanPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLanePlanPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLanePlanPolicy> = inner.list()
}

fun newCachedCappedLanePlanPolicyStore(inner: CappedLanePlanPolicyStore, ttl: Int, clock: Clock): CappedLanePlanPolicyStore {
    return CachedCappedLanePlanPolicyStore(
        inner = inner,
        cache = newTtlCache<CappedLanePlanPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped lane plan policies. Empty fields match all. */
data class CappedLanePlanPolicyQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLanePlanPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedLanePlanPolicy>): MutableList<CappedLanePlanPolicy> {
        val matched = filter(items) { candidate: CappedLanePlanPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLanePlanPolicies(store: CappedLanePlanPolicyStore, query: CappedLanePlanPolicyQuery): MutableList<CappedLanePlanPolicy> {
    return query.apply(store.list())
}
