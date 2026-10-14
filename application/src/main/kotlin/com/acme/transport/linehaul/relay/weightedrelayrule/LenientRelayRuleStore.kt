package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient relay rules by code. */
interface LenientRelayRuleStore {
    fun find(code: String): LenientRelayRule?
    fun save(item: LenientRelayRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRelayRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRelayRuleStore(
    var items: MutableMap<String, LenientRelayRule>,
    var order: MutableList<String>,
) : LenientRelayRuleStore {
    override fun find(code: String): LenientRelayRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRelayRule) {
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

    override fun list(): MutableList<LenientRelayRule> {
        val out = mutableListOf<LenientRelayRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRelayRuleStore(): LenientRelayRuleStore {
    return MemoryLenientRelayRuleStore(mutableMapOf<String, LenientRelayRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRelayRuleStore(
    var inner: LenientRelayRuleStore,
    var cache: TtlCache<LenientRelayRule>,
    var hits: Int,
    var misses: Int,
) : LenientRelayRuleStore {
    override fun find(code: String): LenientRelayRule? {
        val hit = (cache.get(code) as LenientRelayRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRelayRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRelayRule> = inner.list()
}

fun newCachedLenientRelayRuleStore(inner: LenientRelayRuleStore, ttl: Int, clock: Clock): LenientRelayRuleStore {
    return CachedLenientRelayRuleStore(
        inner = inner,
        cache = newTtlCache<LenientRelayRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient relay rules. Empty fields match all. */
data class LenientRelayRuleQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRelayRule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRelayRule>): MutableList<LenientRelayRule> {
        val matched = filter(items) { candidate: LenientRelayRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRelayRules(store: LenientRelayRuleStore, query: LenientRelayRuleQuery): MutableList<LenientRelayRule> {
    return query.apply(store.list())
}
