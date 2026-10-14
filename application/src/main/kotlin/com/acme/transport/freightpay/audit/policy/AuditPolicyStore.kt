package com.acme.transport.freightpay.audit.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit policies by code. */
interface AuditPolicyStore {
    fun find(code: String): AuditPolicy?
    fun save(item: AuditPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditPolicyStore(
    var items: MutableMap<String, AuditPolicy>,
    var order: MutableList<String>,
) : AuditPolicyStore {
    override fun find(code: String): AuditPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditPolicy) {
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

    override fun list(): MutableList<AuditPolicy> {
        val out = mutableListOf<AuditPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditPolicyStore(): AuditPolicyStore {
    return MemoryAuditPolicyStore(mutableMapOf<String, AuditPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditPolicyStore(
    var inner: AuditPolicyStore,
    var cache: TtlCache<AuditPolicy>,
    var hits: Int,
    var misses: Int,
) : AuditPolicyStore {
    override fun find(code: String): AuditPolicy? {
        val hit = (cache.get(code) as AuditPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditPolicy> = inner.list()
}

fun newCachedAuditPolicyStore(inner: AuditPolicyStore, ttl: Int, clock: Clock): AuditPolicyStore {
    return CachedAuditPolicyStore(
        inner = inner,
        cache = newTtlCache<AuditPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit policies. Empty fields match all. */
data class AuditPolicyQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditPolicy>): MutableList<AuditPolicy> {
        val matched = filter(items) { candidate: AuditPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditPolicies(store: AuditPolicyStore, query: AuditPolicyQuery): MutableList<AuditPolicy> {
    return query.apply(store.list())
}
