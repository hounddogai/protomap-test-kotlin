package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient lane plan rules by code. */
interface LenientLanePlanRuleStore {
    fun find(code: String): LenientLanePlanRule?
    fun save(item: LenientLanePlanRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientLanePlanRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientLanePlanRuleStore(
    var items: MutableMap<String, LenientLanePlanRule>,
    var order: MutableList<String>,
) : LenientLanePlanRuleStore {
    override fun find(code: String): LenientLanePlanRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientLanePlanRule) {
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

    override fun list(): MutableList<LenientLanePlanRule> {
        val out = mutableListOf<LenientLanePlanRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientLanePlanRuleStore(): LenientLanePlanRuleStore {
    return MemoryLenientLanePlanRuleStore(mutableMapOf<String, LenientLanePlanRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientLanePlanRuleStore(
    var inner: LenientLanePlanRuleStore,
    var cache: TtlCache<LenientLanePlanRule>,
    var hits: Int,
    var misses: Int,
) : LenientLanePlanRuleStore {
    override fun find(code: String): LenientLanePlanRule? {
        val hit = (cache.get(code) as LenientLanePlanRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientLanePlanRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientLanePlanRule> = inner.list()
}

fun newCachedLenientLanePlanRuleStore(inner: LenientLanePlanRuleStore, ttl: Int, clock: Clock): LenientLanePlanRuleStore {
    return CachedLenientLanePlanRuleStore(
        inner = inner,
        cache = newTtlCache<LenientLanePlanRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient lane plan rules. Empty fields match all. */
data class LenientLanePlanRuleQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientLanePlanRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientLanePlanRule>): MutableList<LenientLanePlanRule> {
        val matched = filter(items) { candidate: LenientLanePlanRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientLanePlanRules(store: LenientLanePlanRuleStore, query: LenientLanePlanRuleQuery): MutableList<LenientLanePlanRule> {
    return query.apply(store.list())
}
