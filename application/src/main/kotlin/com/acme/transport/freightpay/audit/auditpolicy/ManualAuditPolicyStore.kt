package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual audit policies by code. */
interface ManualAuditPolicyStore {
    fun find(code: String): ManualAuditPolicy?
    fun save(item: ManualAuditPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAuditPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAuditPolicyStore(
    var items: MutableMap<String, ManualAuditPolicy>,
    var order: MutableList<String>,
) : ManualAuditPolicyStore {
    override fun find(code: String): ManualAuditPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAuditPolicy) {
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

    override fun list(): MutableList<ManualAuditPolicy> {
        val out = mutableListOf<ManualAuditPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAuditPolicyStore(): ManualAuditPolicyStore {
    return MemoryManualAuditPolicyStore(mutableMapOf<String, ManualAuditPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAuditPolicyStore(
    var inner: ManualAuditPolicyStore,
    var cache: TtlCache<ManualAuditPolicy>,
    var hits: Int,
    var misses: Int,
) : ManualAuditPolicyStore {
    override fun find(code: String): ManualAuditPolicy? {
        val hit = (cache.get(code) as ManualAuditPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAuditPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAuditPolicy> = inner.list()
}

fun newCachedManualAuditPolicyStore(inner: ManualAuditPolicyStore, ttl: Int, clock: Clock): ManualAuditPolicyStore {
    return CachedManualAuditPolicyStore(
        inner = inner,
        cache = newTtlCache<ManualAuditPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual audit policies. Empty fields match all. */
data class ManualAuditPolicyQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAuditPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAuditPolicy>): MutableList<ManualAuditPolicy> {
        val matched = filter(items) { candidate: ManualAuditPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAuditPolicies(store: ManualAuditPolicyStore, query: ManualAuditPolicyQuery): MutableList<ManualAuditPolicy> {
    return query.apply(store.list())
}
