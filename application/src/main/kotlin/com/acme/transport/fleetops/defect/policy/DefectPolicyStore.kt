package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect policies by code. */
interface DefectPolicyStore {
    fun find(code: String): DefectPolicy?
    fun save(item: DefectPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectPolicyStore(
    var items: MutableMap<String, DefectPolicy>,
    var order: MutableList<String>,
) : DefectPolicyStore {
    override fun find(code: String): DefectPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectPolicy) {
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

    override fun list(): MutableList<DefectPolicy> {
        val out = mutableListOf<DefectPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectPolicyStore(): DefectPolicyStore {
    return MemoryDefectPolicyStore(mutableMapOf<String, DefectPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectPolicyStore(
    var inner: DefectPolicyStore,
    var cache: TtlCache<DefectPolicy>,
    var hits: Int,
    var misses: Int,
) : DefectPolicyStore {
    override fun find(code: String): DefectPolicy? {
        val hit = (cache.get(code) as DefectPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectPolicy> = inner.list()
}

fun newCachedDefectPolicyStore(inner: DefectPolicyStore, ttl: Int, clock: Clock): DefectPolicyStore {
    return CachedDefectPolicyStore(
        inner = inner,
        cache = newTtlCache<DefectPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect policies. Empty fields match all. */
data class DefectPolicyQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectPolicy>): MutableList<DefectPolicy> {
        val matched = filter(items) { candidate: DefectPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectPolicies(store: DefectPolicyStore, query: DefectPolicyQuery): MutableList<DefectPolicy> {
    return query.apply(store.list())
}
