package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched lease return rules by code. */
interface BatchedLeaseReturnRuleStore {
    fun find(code: String): BatchedLeaseReturnRule?
    fun save(item: BatchedLeaseReturnRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLeaseReturnRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLeaseReturnRuleStore(
    var items: MutableMap<String, BatchedLeaseReturnRule>,
    var order: MutableList<String>,
) : BatchedLeaseReturnRuleStore {
    override fun find(code: String): BatchedLeaseReturnRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLeaseReturnRule) {
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

    override fun list(): MutableList<BatchedLeaseReturnRule> {
        val out = mutableListOf<BatchedLeaseReturnRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLeaseReturnRuleStore(): BatchedLeaseReturnRuleStore {
    return MemoryBatchedLeaseReturnRuleStore(
        mutableMapOf<String, BatchedLeaseReturnRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLeaseReturnRuleStore(
    var inner: BatchedLeaseReturnRuleStore,
    var cache: TtlCache<BatchedLeaseReturnRule>,
    var hits: Int,
    var misses: Int,
) : BatchedLeaseReturnRuleStore {
    override fun find(code: String): BatchedLeaseReturnRule? {
        val hit = (cache.get(code) as BatchedLeaseReturnRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLeaseReturnRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLeaseReturnRule> = inner.list()
}

fun newCachedBatchedLeaseReturnRuleStore(inner: BatchedLeaseReturnRuleStore, ttl: Int, clock: Clock): BatchedLeaseReturnRuleStore {
    return CachedBatchedLeaseReturnRuleStore(
        inner = inner,
        cache = newTtlCache<BatchedLeaseReturnRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched lease return rules. Empty fields match all. */
data class BatchedLeaseReturnRuleQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLeaseReturnRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLeaseReturnRule>): MutableList<BatchedLeaseReturnRule> {
        val matched = filter(items) { candidate: BatchedLeaseReturnRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLeaseReturnRules(store: BatchedLeaseReturnRuleStore, query: BatchedLeaseReturnRuleQuery): MutableList<BatchedLeaseReturnRule> {
    return query.apply(store.list())
}
