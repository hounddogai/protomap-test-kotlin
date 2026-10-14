package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub rules by code. */
interface HubRuleStore {
    fun find(code: String): HubRule?
    fun save(item: HubRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubRuleStore(
    var items: MutableMap<String, HubRule>,
    var order: MutableList<String>,
) : HubRuleStore {
    override fun find(code: String): HubRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubRule) {
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

    override fun list(): MutableList<HubRule> {
        val out = mutableListOf<HubRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubRuleStore(): HubRuleStore {
    return MemoryHubRuleStore(mutableMapOf<String, HubRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubRuleStore(
    var inner: HubRuleStore,
    var cache: TtlCache<HubRule>,
    var hits: Int,
    var misses: Int,
) : HubRuleStore {
    override fun find(code: String): HubRule? {
        val hit = (cache.get(code) as HubRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubRule> = inner.list()
}

fun newCachedHubRuleStore(inner: HubRuleStore, ttl: Int, clock: Clock): HubRuleStore {
    return CachedHubRuleStore(
        inner = inner,
        cache = newTtlCache<HubRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub rules. Empty fields match all. */
data class HubRuleQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubRule>): MutableList<HubRule> {
        val matched = filter(items) { candidate: HubRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubRules(store: HubRuleStore, query: HubRuleQuery): MutableList<HubRule> = query.apply(store.list())
