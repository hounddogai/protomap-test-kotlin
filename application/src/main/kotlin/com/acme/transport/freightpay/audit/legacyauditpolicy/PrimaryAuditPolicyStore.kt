package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary audit policies by code. */
interface PrimaryAuditPolicyStore {
    fun find(code: String): PrimaryAuditPolicy?
    fun save(item: PrimaryAuditPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAuditPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAuditPolicyStore(
    var items: MutableMap<String, PrimaryAuditPolicy>,
    var order: MutableList<String>,
) : PrimaryAuditPolicyStore {
    override fun find(code: String): PrimaryAuditPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAuditPolicy) {
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

    override fun list(): MutableList<PrimaryAuditPolicy> {
        val out = mutableListOf<PrimaryAuditPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAuditPolicyStore(): PrimaryAuditPolicyStore {
    return MemoryPrimaryAuditPolicyStore(mutableMapOf<String, PrimaryAuditPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAuditPolicyStore(
    var inner: PrimaryAuditPolicyStore,
    var cache: TtlCache<PrimaryAuditPolicy>,
    var hits: Int,
    var misses: Int,
) : PrimaryAuditPolicyStore {
    override fun find(code: String): PrimaryAuditPolicy? {
        val hit = (cache.get(code) as PrimaryAuditPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAuditPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAuditPolicy> = inner.list()
}

fun newCachedPrimaryAuditPolicyStore(inner: PrimaryAuditPolicyStore, ttl: Int, clock: Clock): PrimaryAuditPolicyStore {
    return CachedPrimaryAuditPolicyStore(
        inner = inner,
        cache = newTtlCache<PrimaryAuditPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary audit policies. Empty fields match all. */
data class PrimaryAuditPolicyQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAuditPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAuditPolicy>): MutableList<PrimaryAuditPolicy> {
        val matched = filter(items) { candidate: PrimaryAuditPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAuditPolicies(store: PrimaryAuditPolicyStore, query: PrimaryAuditPolicyQuery): MutableList<PrimaryAuditPolicy> {
    return query.apply(store.list())
}
