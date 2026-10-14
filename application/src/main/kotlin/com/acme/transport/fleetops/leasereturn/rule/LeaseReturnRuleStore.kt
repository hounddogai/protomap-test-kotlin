package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return rules by code. */
interface LeaseReturnRuleStore {
    fun find(code: String): LeaseReturnRule?
    fun save(item: LeaseReturnRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnRuleStore(
    var items: MutableMap<String, LeaseReturnRule>,
    var order: MutableList<String>,
) : LeaseReturnRuleStore {
    override fun find(code: String): LeaseReturnRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnRule) {
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

    override fun list(): MutableList<LeaseReturnRule> {
        val out = mutableListOf<LeaseReturnRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnRuleStore(): LeaseReturnRuleStore {
    return MemoryLeaseReturnRuleStore(mutableMapOf<String, LeaseReturnRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnRuleStore(
    var inner: LeaseReturnRuleStore,
    var cache: TtlCache<LeaseReturnRule>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnRuleStore {
    override fun find(code: String): LeaseReturnRule? {
        val hit = (cache.get(code) as LeaseReturnRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnRule> = inner.list()
}

fun newCachedLeaseReturnRuleStore(inner: LeaseReturnRuleStore, ttl: Int, clock: Clock): LeaseReturnRuleStore {
    return CachedLeaseReturnRuleStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return rules. Empty fields match all. */
data class LeaseReturnRuleQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnRule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnRule>): MutableList<LeaseReturnRule> {
        val matched = filter(items) { candidate: LeaseReturnRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnRules(store: LeaseReturnRuleStore, query: LeaseReturnRuleQuery): MutableList<LeaseReturnRule> {
    return query.apply(store.list())
}
