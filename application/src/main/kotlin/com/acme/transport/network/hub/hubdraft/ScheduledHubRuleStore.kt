package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled hub rules by code. */
interface ScheduledHubRuleStore {
    fun find(code: String): ScheduledHubRule?
    fun save(item: ScheduledHubRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHubRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHubRuleStore(
    var items: MutableMap<String, ScheduledHubRule>,
    var order: MutableList<String>,
) : ScheduledHubRuleStore {
    override fun find(code: String): ScheduledHubRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHubRule) {
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

    override fun list(): MutableList<ScheduledHubRule> {
        val out = mutableListOf<ScheduledHubRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHubRuleStore(): ScheduledHubRuleStore {
    return MemoryScheduledHubRuleStore(mutableMapOf<String, ScheduledHubRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHubRuleStore(
    var inner: ScheduledHubRuleStore,
    var cache: TtlCache<ScheduledHubRule>,
    var hits: Int,
    var misses: Int,
) : ScheduledHubRuleStore {
    override fun find(code: String): ScheduledHubRule? {
        val hit = (cache.get(code) as ScheduledHubRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHubRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHubRule> = inner.list()
}

fun newCachedScheduledHubRuleStore(inner: ScheduledHubRuleStore, ttl: Int, clock: Clock): ScheduledHubRuleStore {
    return CachedScheduledHubRuleStore(
        inner = inner,
        cache = newTtlCache<ScheduledHubRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hub rules. Empty fields match all. */
data class ScheduledHubRuleQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHubRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHubRule>): MutableList<ScheduledHubRule> {
        val matched = filter(items) { candidate: ScheduledHubRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHubRules(store: ScheduledHubRuleStore, query: ScheduledHubRuleQuery): MutableList<ScheduledHubRule> {
    return query.apply(store.list())
}
