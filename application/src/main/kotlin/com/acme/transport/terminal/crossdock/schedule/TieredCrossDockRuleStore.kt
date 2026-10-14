package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered cross dock rules by code. */
interface TieredCrossDockRuleStore {
    fun find(code: String): TieredCrossDockRule?
    fun save(item: TieredCrossDockRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCrossDockRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCrossDockRuleStore(
    var items: MutableMap<String, TieredCrossDockRule>,
    var order: MutableList<String>,
) : TieredCrossDockRuleStore {
    override fun find(code: String): TieredCrossDockRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCrossDockRule) {
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

    override fun list(): MutableList<TieredCrossDockRule> {
        val out = mutableListOf<TieredCrossDockRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCrossDockRuleStore(): TieredCrossDockRuleStore {
    return MemoryTieredCrossDockRuleStore(mutableMapOf<String, TieredCrossDockRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCrossDockRuleStore(
    var inner: TieredCrossDockRuleStore,
    var cache: TtlCache<TieredCrossDockRule>,
    var hits: Int,
    var misses: Int,
) : TieredCrossDockRuleStore {
    override fun find(code: String): TieredCrossDockRule? {
        val hit = (cache.get(code) as TieredCrossDockRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCrossDockRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCrossDockRule> = inner.list()
}

fun newCachedTieredCrossDockRuleStore(inner: TieredCrossDockRuleStore, ttl: Int, clock: Clock): TieredCrossDockRuleStore {
    return CachedTieredCrossDockRuleStore(
        inner = inner,
        cache = newTtlCache<TieredCrossDockRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cross dock rules. Empty fields match all. */
data class TieredCrossDockRuleQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCrossDockRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCrossDockRule>): MutableList<TieredCrossDockRule> {
        val matched = filter(items) { candidate: TieredCrossDockRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCrossDockRules(store: TieredCrossDockRuleStore, query: TieredCrossDockRuleQuery): MutableList<TieredCrossDockRule> {
    return query.apply(store.list())
}
