package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched availability rules by code. */
interface BatchedAvailabilityRuleStore {
    fun find(code: String): BatchedAvailabilityRule?
    fun save(item: BatchedAvailabilityRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAvailabilityRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAvailabilityRuleStore(
    var items: MutableMap<String, BatchedAvailabilityRule>,
    var order: MutableList<String>,
) : BatchedAvailabilityRuleStore {
    override fun find(code: String): BatchedAvailabilityRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAvailabilityRule) {
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

    override fun list(): MutableList<BatchedAvailabilityRule> {
        val out = mutableListOf<BatchedAvailabilityRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAvailabilityRuleStore(): BatchedAvailabilityRuleStore {
    return MemoryBatchedAvailabilityRuleStore(
        mutableMapOf<String, BatchedAvailabilityRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAvailabilityRuleStore(
    var inner: BatchedAvailabilityRuleStore,
    var cache: TtlCache<BatchedAvailabilityRule>,
    var hits: Int,
    var misses: Int,
) : BatchedAvailabilityRuleStore {
    override fun find(code: String): BatchedAvailabilityRule? {
        val hit = (cache.get(code) as BatchedAvailabilityRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAvailabilityRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAvailabilityRule> = inner.list()
}

fun newCachedBatchedAvailabilityRuleStore(inner: BatchedAvailabilityRuleStore, ttl: Int, clock: Clock): BatchedAvailabilityRuleStore {
    return CachedBatchedAvailabilityRuleStore(
        inner = inner,
        cache = newTtlCache<BatchedAvailabilityRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched availability rules. Empty fields match all. */
data class BatchedAvailabilityRuleQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAvailabilityRule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAvailabilityRule>): MutableList<BatchedAvailabilityRule> {
        val matched = filter(items) { candidate: BatchedAvailabilityRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAvailabilityRules(store: BatchedAvailabilityRuleStore, query: BatchedAvailabilityRuleQuery): MutableList<BatchedAvailabilityRule> {
    return query.apply(store.list())
}
