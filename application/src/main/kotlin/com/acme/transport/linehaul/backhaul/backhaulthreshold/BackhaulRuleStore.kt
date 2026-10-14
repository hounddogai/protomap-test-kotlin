package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul rules by code. */
interface BackhaulRuleStore {
    fun find(code: String): BackhaulRule?
    fun save(item: BackhaulRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulRuleStore(
    var items: MutableMap<String, BackhaulRule>,
    var order: MutableList<String>,
) : BackhaulRuleStore {
    override fun find(code: String): BackhaulRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulRule) {
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

    override fun list(): MutableList<BackhaulRule> {
        val out = mutableListOf<BackhaulRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulRuleStore(): BackhaulRuleStore {
    return MemoryBackhaulRuleStore(mutableMapOf<String, BackhaulRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulRuleStore(
    var inner: BackhaulRuleStore,
    var cache: TtlCache<BackhaulRule>,
    var hits: Int,
    var misses: Int,
) : BackhaulRuleStore {
    override fun find(code: String): BackhaulRule? {
        val hit = (cache.get(code) as BackhaulRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulRule> = inner.list()
}

fun newCachedBackhaulRuleStore(inner: BackhaulRuleStore, ttl: Int, clock: Clock): BackhaulRuleStore {
    return CachedBackhaulRuleStore(
        inner = inner,
        cache = newTtlCache<BackhaulRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul rules. Empty fields match all. */
data class BackhaulRuleQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulRule>): MutableList<BackhaulRule> {
        val matched = filter(items) { candidate: BackhaulRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulRules(store: BackhaulRuleStore, query: BackhaulRuleQuery): MutableList<BackhaulRule> {
    return query.apply(store.list())
}
