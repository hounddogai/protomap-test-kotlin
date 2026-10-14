package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled service area rules by code. */
interface ScheduledServiceAreaRuleStore {
    fun find(code: String): ScheduledServiceAreaRule?
    fun save(item: ScheduledServiceAreaRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledServiceAreaRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledServiceAreaRuleStore(
    var items: MutableMap<String, ScheduledServiceAreaRule>,
    var order: MutableList<String>,
) : ScheduledServiceAreaRuleStore {
    override fun find(code: String): ScheduledServiceAreaRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledServiceAreaRule) {
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

    override fun list(): MutableList<ScheduledServiceAreaRule> {
        val out = mutableListOf<ScheduledServiceAreaRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledServiceAreaRuleStore(): ScheduledServiceAreaRuleStore {
    return MemoryScheduledServiceAreaRuleStore(
        mutableMapOf<String, ScheduledServiceAreaRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledServiceAreaRuleStore(
    var inner: ScheduledServiceAreaRuleStore,
    var cache: TtlCache<ScheduledServiceAreaRule>,
    var hits: Int,
    var misses: Int,
) : ScheduledServiceAreaRuleStore {
    override fun find(code: String): ScheduledServiceAreaRule? {
        val hit = (cache.get(code) as ScheduledServiceAreaRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledServiceAreaRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledServiceAreaRule> = inner.list()
}

fun newCachedScheduledServiceAreaRuleStore(inner: ScheduledServiceAreaRuleStore, ttl: Int, clock: Clock): ScheduledServiceAreaRuleStore {
    return CachedScheduledServiceAreaRuleStore(
        inner = inner,
        cache = newTtlCache<ScheduledServiceAreaRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled service area rules. Empty fields match all. */
data class ScheduledServiceAreaRuleQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledServiceAreaRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledServiceAreaRule>): MutableList<ScheduledServiceAreaRule> {
        val matched = filter(items) { candidate: ScheduledServiceAreaRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledServiceAreaRules(store: ScheduledServiceAreaRuleStore, query: ScheduledServiceAreaRuleQuery): MutableList<ScheduledServiceAreaRule> {
    return query.apply(store.list())
}
