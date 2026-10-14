package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time rules by code. */
interface TransitTimeRuleStore {
    fun find(code: String): TransitTimeRule?
    fun save(item: TransitTimeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeRuleStore(
    var items: MutableMap<String, TransitTimeRule>,
    var order: MutableList<String>,
) : TransitTimeRuleStore {
    override fun find(code: String): TransitTimeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeRule) {
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

    override fun list(): MutableList<TransitTimeRule> {
        val out = mutableListOf<TransitTimeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeRuleStore(): TransitTimeRuleStore {
    return MemoryTransitTimeRuleStore(mutableMapOf<String, TransitTimeRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeRuleStore(
    var inner: TransitTimeRuleStore,
    var cache: TtlCache<TransitTimeRule>,
    var hits: Int,
    var misses: Int,
) : TransitTimeRuleStore {
    override fun find(code: String): TransitTimeRule? {
        val hit = (cache.get(code) as TransitTimeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeRule> = inner.list()
}

fun newCachedTransitTimeRuleStore(inner: TransitTimeRuleStore, ttl: Int, clock: Clock): TransitTimeRuleStore {
    return CachedTransitTimeRuleStore(
        inner = inner,
        cache = newTtlCache<TransitTimeRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time rules. Empty fields match all. */
data class TransitTimeRuleQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeRule>): MutableList<TransitTimeRule> {
        val matched = filter(items) { candidate: TransitTimeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeRules(store: TransitTimeRuleStore, query: TransitTimeRuleQuery): MutableList<TransitTimeRule> {
    return query.apply(store.list())
}
