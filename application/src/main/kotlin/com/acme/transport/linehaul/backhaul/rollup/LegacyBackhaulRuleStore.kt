package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy backhaul rules by code. */
interface LegacyBackhaulRuleStore {
    fun find(code: String): LegacyBackhaulRule?
    fun save(item: LegacyBackhaulRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBackhaulRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBackhaulRuleStore(
    var items: MutableMap<String, LegacyBackhaulRule>,
    var order: MutableList<String>,
) : LegacyBackhaulRuleStore {
    override fun find(code: String): LegacyBackhaulRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBackhaulRule) {
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

    override fun list(): MutableList<LegacyBackhaulRule> {
        val out = mutableListOf<LegacyBackhaulRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBackhaulRuleStore(): LegacyBackhaulRuleStore {
    return MemoryLegacyBackhaulRuleStore(mutableMapOf<String, LegacyBackhaulRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBackhaulRuleStore(
    var inner: LegacyBackhaulRuleStore,
    var cache: TtlCache<LegacyBackhaulRule>,
    var hits: Int,
    var misses: Int,
) : LegacyBackhaulRuleStore {
    override fun find(code: String): LegacyBackhaulRule? {
        val hit = (cache.get(code) as LegacyBackhaulRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBackhaulRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBackhaulRule> = inner.list()
}

fun newCachedLegacyBackhaulRuleStore(inner: LegacyBackhaulRuleStore, ttl: Int, clock: Clock): LegacyBackhaulRuleStore {
    return CachedLegacyBackhaulRuleStore(
        inner = inner,
        cache = newTtlCache<LegacyBackhaulRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy backhaul rules. Empty fields match all. */
data class LegacyBackhaulRuleQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBackhaulRule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBackhaulRule>): MutableList<LegacyBackhaulRule> {
        val matched = filter(items) { candidate: LegacyBackhaulRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBackhaulRules(store: LegacyBackhaulRuleStore, query: LegacyBackhaulRuleQuery): MutableList<LegacyBackhaulRule> {
    return query.apply(store.list())
}
