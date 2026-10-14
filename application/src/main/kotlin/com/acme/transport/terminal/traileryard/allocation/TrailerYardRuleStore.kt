package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard rules by code. */
interface TrailerYardRuleStore {
    fun find(code: String): TrailerYardRule?
    fun save(item: TrailerYardRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardRuleStore(
    var items: MutableMap<String, TrailerYardRule>,
    var order: MutableList<String>,
) : TrailerYardRuleStore {
    override fun find(code: String): TrailerYardRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardRule) {
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

    override fun list(): MutableList<TrailerYardRule> {
        val out = mutableListOf<TrailerYardRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardRuleStore(): TrailerYardRuleStore {
    return MemoryTrailerYardRuleStore(mutableMapOf<String, TrailerYardRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardRuleStore(
    var inner: TrailerYardRuleStore,
    var cache: TtlCache<TrailerYardRule>,
    var hits: Int,
    var misses: Int,
) : TrailerYardRuleStore {
    override fun find(code: String): TrailerYardRule? {
        val hit = (cache.get(code) as TrailerYardRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardRule> = inner.list()
}

fun newCachedTrailerYardRuleStore(inner: TrailerYardRuleStore, ttl: Int, clock: Clock): TrailerYardRuleStore {
    return CachedTrailerYardRuleStore(
        inner = inner,
        cache = newTtlCache<TrailerYardRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard rules. Empty fields match all. */
data class TrailerYardRuleQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardRule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardRule>): MutableList<TrailerYardRule> {
        val matched = filter(items) { candidate: TrailerYardRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardRules(store: TrailerYardRuleStore, query: TrailerYardRuleQuery): MutableList<TrailerYardRule> {
    return query.apply(store.list())
}
