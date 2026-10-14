package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge rules by code. */
interface EvChargeRuleStore {
    fun find(code: String): EvChargeRule?
    fun save(item: EvChargeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeRuleStore(
    var items: MutableMap<String, EvChargeRule>,
    var order: MutableList<String>,
) : EvChargeRuleStore {
    override fun find(code: String): EvChargeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeRule) {
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

    override fun list(): MutableList<EvChargeRule> {
        val out = mutableListOf<EvChargeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeRuleStore(): EvChargeRuleStore {
    return MemoryEvChargeRuleStore(mutableMapOf<String, EvChargeRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeRuleStore(
    var inner: EvChargeRuleStore,
    var cache: TtlCache<EvChargeRule>,
    var hits: Int,
    var misses: Int,
) : EvChargeRuleStore {
    override fun find(code: String): EvChargeRule? {
        val hit = (cache.get(code) as EvChargeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeRule> = inner.list()
}

fun newCachedEvChargeRuleStore(inner: EvChargeRuleStore, ttl: Int, clock: Clock): EvChargeRuleStore {
    return CachedEvChargeRuleStore(
        inner = inner,
        cache = newTtlCache<EvChargeRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge rules. Empty fields match all. */
data class EvChargeRuleQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeRule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeRule>): MutableList<EvChargeRule> {
        val matched = filter(items) { candidate: EvChargeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeRules(store: EvChargeRuleStore, query: EvChargeRuleQuery): MutableList<EvChargeRule> {
    return query.apply(store.list())
}
