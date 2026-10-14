package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery rules by code. */
interface RedeliveryRuleStore {
    fun find(code: String): RedeliveryRule?
    fun save(item: RedeliveryRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryRuleStore(
    var items: MutableMap<String, RedeliveryRule>,
    var order: MutableList<String>,
) : RedeliveryRuleStore {
    override fun find(code: String): RedeliveryRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryRule) {
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

    override fun list(): MutableList<RedeliveryRule> {
        val out = mutableListOf<RedeliveryRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryRuleStore(): RedeliveryRuleStore {
    return MemoryRedeliveryRuleStore(mutableMapOf<String, RedeliveryRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryRuleStore(
    var inner: RedeliveryRuleStore,
    var cache: TtlCache<RedeliveryRule>,
    var hits: Int,
    var misses: Int,
) : RedeliveryRuleStore {
    override fun find(code: String): RedeliveryRule? {
        val hit = (cache.get(code) as RedeliveryRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryRule> = inner.list()
}

fun newCachedRedeliveryRuleStore(inner: RedeliveryRuleStore, ttl: Int, clock: Clock): RedeliveryRuleStore {
    return CachedRedeliveryRuleStore(
        inner = inner,
        cache = newTtlCache<RedeliveryRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery rules. Empty fields match all. */
data class RedeliveryRuleQuery(
    var depot: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryRule): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryRule>): MutableList<RedeliveryRule> {
        val matched = filter(items) { candidate: RedeliveryRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryRules(store: RedeliveryRuleStore, query: RedeliveryRuleQuery): MutableList<RedeliveryRule> {
    return query.apply(store.list())
}
