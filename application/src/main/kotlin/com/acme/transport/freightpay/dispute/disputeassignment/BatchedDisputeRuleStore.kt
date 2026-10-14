package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched dispute rules by code. */
interface BatchedDisputeRuleStore {
    fun find(code: String): BatchedDisputeRule?
    fun save(item: BatchedDisputeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDisputeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDisputeRuleStore(
    var items: MutableMap<String, BatchedDisputeRule>,
    var order: MutableList<String>,
) : BatchedDisputeRuleStore {
    override fun find(code: String): BatchedDisputeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDisputeRule) {
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

    override fun list(): MutableList<BatchedDisputeRule> {
        val out = mutableListOf<BatchedDisputeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDisputeRuleStore(): BatchedDisputeRuleStore {
    return MemoryBatchedDisputeRuleStore(mutableMapOf<String, BatchedDisputeRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDisputeRuleStore(
    var inner: BatchedDisputeRuleStore,
    var cache: TtlCache<BatchedDisputeRule>,
    var hits: Int,
    var misses: Int,
) : BatchedDisputeRuleStore {
    override fun find(code: String): BatchedDisputeRule? {
        val hit = (cache.get(code) as BatchedDisputeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDisputeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDisputeRule> = inner.list()
}

fun newCachedBatchedDisputeRuleStore(inner: BatchedDisputeRuleStore, ttl: Int, clock: Clock): BatchedDisputeRuleStore {
    return CachedBatchedDisputeRuleStore(
        inner = inner,
        cache = newTtlCache<BatchedDisputeRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dispute rules. Empty fields match all. */
data class BatchedDisputeRuleQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDisputeRule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDisputeRule>): MutableList<BatchedDisputeRule> {
        val matched = filter(items) { candidate: BatchedDisputeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDisputeRules(store: BatchedDisputeRuleStore, query: BatchedDisputeRuleQuery): MutableList<BatchedDisputeRule> {
    return query.apply(store.list())
}
