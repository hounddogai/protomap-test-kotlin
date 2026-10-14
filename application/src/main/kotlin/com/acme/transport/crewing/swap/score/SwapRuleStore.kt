package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap rules by code. */
interface SwapRuleStore {
    fun find(code: String): SwapRule?
    fun save(item: SwapRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapRuleStore(
    var items: MutableMap<String, SwapRule>,
    var order: MutableList<String>,
) : SwapRuleStore {
    override fun find(code: String): SwapRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapRule) {
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

    override fun list(): MutableList<SwapRule> {
        val out = mutableListOf<SwapRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapRuleStore(): SwapRuleStore {
    return MemorySwapRuleStore(mutableMapOf<String, SwapRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapRuleStore(
    var inner: SwapRuleStore,
    var cache: TtlCache<SwapRule>,
    var hits: Int,
    var misses: Int,
) : SwapRuleStore {
    override fun find(code: String): SwapRule? {
        val hit = (cache.get(code) as SwapRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapRule> = inner.list()
}

fun newCachedSwapRuleStore(inner: SwapRuleStore, ttl: Int, clock: Clock): SwapRuleStore {
    return CachedSwapRuleStore(
        inner = inner,
        cache = newTtlCache<SwapRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap rules. Empty fields match all. */
data class SwapRuleQuery(
    var depot: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapRule): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<SwapRule>): MutableList<SwapRule> {
        val matched = filter(items) { candidate: SwapRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapRules(store: SwapRuleStore, query: SwapRuleQuery): MutableList<SwapRule> = query.apply(store.list())
