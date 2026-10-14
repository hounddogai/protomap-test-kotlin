package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual availability rules by code. */
interface ManualAvailabilityRuleStore {
    fun find(code: String): ManualAvailabilityRule?
    fun save(item: ManualAvailabilityRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAvailabilityRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAvailabilityRuleStore(
    var items: MutableMap<String, ManualAvailabilityRule>,
    var order: MutableList<String>,
) : ManualAvailabilityRuleStore {
    override fun find(code: String): ManualAvailabilityRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAvailabilityRule) {
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

    override fun list(): MutableList<ManualAvailabilityRule> {
        val out = mutableListOf<ManualAvailabilityRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAvailabilityRuleStore(): ManualAvailabilityRuleStore {
    return MemoryManualAvailabilityRuleStore(
        mutableMapOf<String, ManualAvailabilityRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAvailabilityRuleStore(
    var inner: ManualAvailabilityRuleStore,
    var cache: TtlCache<ManualAvailabilityRule>,
    var hits: Int,
    var misses: Int,
) : ManualAvailabilityRuleStore {
    override fun find(code: String): ManualAvailabilityRule? {
        val hit = (cache.get(code) as ManualAvailabilityRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAvailabilityRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAvailabilityRule> = inner.list()
}

fun newCachedManualAvailabilityRuleStore(inner: ManualAvailabilityRuleStore, ttl: Int, clock: Clock): ManualAvailabilityRuleStore {
    return CachedManualAvailabilityRuleStore(
        inner = inner,
        cache = newTtlCache<ManualAvailabilityRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual availability rules. Empty fields match all. */
data class ManualAvailabilityRuleQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAvailabilityRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ManualAvailabilityRule>): MutableList<ManualAvailabilityRule> {
        val matched = filter(items) { candidate: ManualAvailabilityRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAvailabilityRules(store: ManualAvailabilityRuleStore, query: ManualAvailabilityRuleQuery): MutableList<ManualAvailabilityRule> {
    return query.apply(store.list())
}
