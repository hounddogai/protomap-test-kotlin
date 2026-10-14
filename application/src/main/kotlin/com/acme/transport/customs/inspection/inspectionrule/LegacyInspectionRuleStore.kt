package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection rules by code. */
interface LegacyInspectionRuleStore {
    fun find(code: String): LegacyInspectionRule?
    fun save(item: LegacyInspectionRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionRuleStore(
    var items: MutableMap<String, LegacyInspectionRule>,
    var order: MutableList<String>,
) : LegacyInspectionRuleStore {
    override fun find(code: String): LegacyInspectionRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionRule) {
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

    override fun list(): MutableList<LegacyInspectionRule> {
        val out = mutableListOf<LegacyInspectionRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionRuleStore(): LegacyInspectionRuleStore {
    return MemoryLegacyInspectionRuleStore(
        mutableMapOf<String, LegacyInspectionRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionRuleStore(
    var inner: LegacyInspectionRuleStore,
    var cache: TtlCache<LegacyInspectionRule>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionRuleStore {
    override fun find(code: String): LegacyInspectionRule? {
        val hit = (cache.get(code) as LegacyInspectionRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionRule> = inner.list()
}

fun newCachedLegacyInspectionRuleStore(inner: LegacyInspectionRuleStore, ttl: Int, clock: Clock): LegacyInspectionRuleStore {
    return CachedLegacyInspectionRuleStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection rules. Empty fields match all. */
data class LegacyInspectionRuleQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyInspectionRule>): MutableList<LegacyInspectionRule> {
        val matched = filter(items) { candidate: LegacyInspectionRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionRules(store: LegacyInspectionRuleStore, query: LegacyInspectionRuleQuery): MutableList<LegacyInspectionRule> {
    return query.apply(store.list())
}
