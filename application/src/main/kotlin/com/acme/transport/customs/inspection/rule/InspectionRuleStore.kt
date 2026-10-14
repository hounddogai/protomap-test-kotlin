package com.acme.transport.customs.inspection.rule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection rules by code. */
interface InspectionRuleStore {
    fun find(code: String): InspectionRule?
    fun save(item: InspectionRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionRuleStore(
    var items: MutableMap<String, InspectionRule>,
    var order: MutableList<String>,
) : InspectionRuleStore {
    override fun find(code: String): InspectionRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionRule) {
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

    override fun list(): MutableList<InspectionRule> {
        val out = mutableListOf<InspectionRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionRuleStore(): InspectionRuleStore {
    return MemoryInspectionRuleStore(mutableMapOf<String, InspectionRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionRuleStore(
    var inner: InspectionRuleStore,
    var cache: TtlCache<InspectionRule>,
    var hits: Int,
    var misses: Int,
) : InspectionRuleStore {
    override fun find(code: String): InspectionRule? {
        val hit = (cache.get(code) as InspectionRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionRule> = inner.list()
}

fun newCachedInspectionRuleStore(inner: InspectionRuleStore, ttl: Int, clock: Clock): InspectionRuleStore {
    return CachedInspectionRuleStore(
        inner = inner,
        cache = newTtlCache<InspectionRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection rules. Empty fields match all. */
data class InspectionRuleQuery(
    var region: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionRule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionRule>): MutableList<InspectionRule> {
        val matched = filter(items) { candidate: InspectionRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionRules(store: InspectionRuleStore, query: InspectionRuleQuery): MutableList<InspectionRule> {
    return query.apply(store.list())
}
