package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered sortation rules by code. */
interface TieredSortationRuleStore {
    fun find(code: String): TieredSortationRule?
    fun save(item: TieredSortationRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSortationRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSortationRuleStore(
    var items: MutableMap<String, TieredSortationRule>,
    var order: MutableList<String>,
) : TieredSortationRuleStore {
    override fun find(code: String): TieredSortationRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSortationRule) {
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

    override fun list(): MutableList<TieredSortationRule> {
        val out = mutableListOf<TieredSortationRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSortationRuleStore(): TieredSortationRuleStore {
    return MemoryTieredSortationRuleStore(mutableMapOf<String, TieredSortationRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSortationRuleStore(
    var inner: TieredSortationRuleStore,
    var cache: TtlCache<TieredSortationRule>,
    var hits: Int,
    var misses: Int,
) : TieredSortationRuleStore {
    override fun find(code: String): TieredSortationRule? {
        val hit = (cache.get(code) as TieredSortationRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSortationRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSortationRule> = inner.list()
}

fun newCachedTieredSortationRuleStore(inner: TieredSortationRuleStore, ttl: Int, clock: Clock): TieredSortationRuleStore {
    return CachedTieredSortationRuleStore(
        inner = inner,
        cache = newTtlCache<TieredSortationRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered sortation rules. Empty fields match all. */
data class TieredSortationRuleQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSortationRule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<TieredSortationRule>): MutableList<TieredSortationRule> {
        val matched = filter(items) { candidate: TieredSortationRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSortationRules(store: TieredSortationRuleStore, query: TieredSortationRuleQuery): MutableList<TieredSortationRule> {
    return query.apply(store.list())
}
