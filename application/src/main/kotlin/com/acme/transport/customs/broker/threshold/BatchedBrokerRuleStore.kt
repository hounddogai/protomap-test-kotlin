package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched broker rules by code. */
interface BatchedBrokerRuleStore {
    fun find(code: String): BatchedBrokerRule?
    fun save(item: BatchedBrokerRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedBrokerRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedBrokerRuleStore(
    var items: MutableMap<String, BatchedBrokerRule>,
    var order: MutableList<String>,
) : BatchedBrokerRuleStore {
    override fun find(code: String): BatchedBrokerRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedBrokerRule) {
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

    override fun list(): MutableList<BatchedBrokerRule> {
        val out = mutableListOf<BatchedBrokerRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedBrokerRuleStore(): BatchedBrokerRuleStore {
    return MemoryBatchedBrokerRuleStore(mutableMapOf<String, BatchedBrokerRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedBrokerRuleStore(
    var inner: BatchedBrokerRuleStore,
    var cache: TtlCache<BatchedBrokerRule>,
    var hits: Int,
    var misses: Int,
) : BatchedBrokerRuleStore {
    override fun find(code: String): BatchedBrokerRule? {
        val hit = (cache.get(code) as BatchedBrokerRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedBrokerRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedBrokerRule> = inner.list()
}

fun newCachedBatchedBrokerRuleStore(inner: BatchedBrokerRuleStore, ttl: Int, clock: Clock): BatchedBrokerRuleStore {
    return CachedBatchedBrokerRuleStore(
        inner = inner,
        cache = newTtlCache<BatchedBrokerRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched broker rules. Empty fields match all. */
data class BatchedBrokerRuleQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedBrokerRule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedBrokerRule>): MutableList<BatchedBrokerRule> {
        val matched = filter(items) { candidate: BatchedBrokerRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedBrokerRules(store: BatchedBrokerRuleStore, query: BatchedBrokerRuleQuery): MutableList<BatchedBrokerRule> {
    return query.apply(store.list())
}
