package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered swap rules by code. */
interface TieredSwapRuleStore {
    fun find(code: String): TieredSwapRule?
    fun save(item: TieredSwapRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSwapRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSwapRuleStore(
    var items: MutableMap<String, TieredSwapRule>,
    var order: MutableList<String>,
) : TieredSwapRuleStore {
    override fun find(code: String): TieredSwapRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSwapRule) {
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

    override fun list(): MutableList<TieredSwapRule> {
        val out = mutableListOf<TieredSwapRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSwapRuleStore(): TieredSwapRuleStore {
    return MemoryTieredSwapRuleStore(mutableMapOf<String, TieredSwapRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSwapRuleStore(
    var inner: TieredSwapRuleStore,
    var cache: TtlCache<TieredSwapRule>,
    var hits: Int,
    var misses: Int,
) : TieredSwapRuleStore {
    override fun find(code: String): TieredSwapRule? {
        val hit = (cache.get(code) as TieredSwapRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSwapRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSwapRule> = inner.list()
}

fun newCachedTieredSwapRuleStore(inner: TieredSwapRuleStore, ttl: Int, clock: Clock): TieredSwapRuleStore {
    return CachedTieredSwapRuleStore(
        inner = inner,
        cache = newTtlCache<TieredSwapRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered swap rules. Empty fields match all. */
data class TieredSwapRuleQuery(
    var region: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSwapRule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredSwapRule>): MutableList<TieredSwapRule> {
        val matched = filter(items) { candidate: TieredSwapRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSwapRules(store: TieredSwapRuleStore, query: TieredSwapRuleQuery): MutableList<TieredSwapRule> {
    return query.apply(store.list())
}
