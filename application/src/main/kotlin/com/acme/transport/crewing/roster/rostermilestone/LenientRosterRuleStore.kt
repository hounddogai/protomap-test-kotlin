package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient roster rules by code. */
interface LenientRosterRuleStore {
    fun find(code: String): LenientRosterRule?
    fun save(item: LenientRosterRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRosterRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRosterRuleStore(
    var items: MutableMap<String, LenientRosterRule>,
    var order: MutableList<String>,
) : LenientRosterRuleStore {
    override fun find(code: String): LenientRosterRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRosterRule) {
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

    override fun list(): MutableList<LenientRosterRule> {
        val out = mutableListOf<LenientRosterRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRosterRuleStore(): LenientRosterRuleStore {
    return MemoryLenientRosterRuleStore(mutableMapOf<String, LenientRosterRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRosterRuleStore(
    var inner: LenientRosterRuleStore,
    var cache: TtlCache<LenientRosterRule>,
    var hits: Int,
    var misses: Int,
) : LenientRosterRuleStore {
    override fun find(code: String): LenientRosterRule? {
        val hit = (cache.get(code) as LenientRosterRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRosterRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRosterRule> = inner.list()
}

fun newCachedLenientRosterRuleStore(inner: LenientRosterRuleStore, ttl: Int, clock: Clock): LenientRosterRuleStore {
    return CachedLenientRosterRuleStore(
        inner = inner,
        cache = newTtlCache<LenientRosterRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient roster rules. Empty fields match all. */
data class LenientRosterRuleQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRosterRule): Boolean {
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

    fun apply(items: MutableList<LenientRosterRule>): MutableList<LenientRosterRule> {
        val matched = filter(items) { candidate: LenientRosterRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRosterRules(store: LenientRosterRuleStore, query: LenientRosterRuleQuery): MutableList<LenientRosterRule> {
    return query.apply(store.list())
}
