package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold rules by code. */
interface HoldRuleStore {
    fun find(code: String): HoldRule?
    fun save(item: HoldRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldRuleStore(
    var items: MutableMap<String, HoldRule>,
    var order: MutableList<String>,
) : HoldRuleStore {
    override fun find(code: String): HoldRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldRule) {
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

    override fun list(): MutableList<HoldRule> {
        val out = mutableListOf<HoldRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldRuleStore(): HoldRuleStore {
    return MemoryHoldRuleStore(mutableMapOf<String, HoldRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldRuleStore(
    var inner: HoldRuleStore,
    var cache: TtlCache<HoldRule>,
    var hits: Int,
    var misses: Int,
) : HoldRuleStore {
    override fun find(code: String): HoldRule? {
        val hit = (cache.get(code) as HoldRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldRule> = inner.list()
}

fun newCachedHoldRuleStore(inner: HoldRuleStore, ttl: Int, clock: Clock): HoldRuleStore {
    return CachedHoldRuleStore(
        inner = inner,
        cache = newTtlCache<HoldRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold rules. Empty fields match all. */
data class HoldRuleQuery(
    var region: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldRule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldRule>): MutableList<HoldRule> {
        val matched = filter(items) { candidate: HoldRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldRules(store: HoldRuleStore, query: HoldRuleQuery): MutableList<HoldRule> = query.apply(store.list())
