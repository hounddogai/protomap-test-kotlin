package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff rules by code. */
interface CutoffRuleStore {
    fun find(code: String): CutoffRule?
    fun save(item: CutoffRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffRuleStore(
    var items: MutableMap<String, CutoffRule>,
    var order: MutableList<String>,
) : CutoffRuleStore {
    override fun find(code: String): CutoffRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffRule) {
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

    override fun list(): MutableList<CutoffRule> {
        val out = mutableListOf<CutoffRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffRuleStore(): CutoffRuleStore {
    return MemoryCutoffRuleStore(mutableMapOf<String, CutoffRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffRuleStore(
    var inner: CutoffRuleStore,
    var cache: TtlCache<CutoffRule>,
    var hits: Int,
    var misses: Int,
) : CutoffRuleStore {
    override fun find(code: String): CutoffRule? {
        val hit = (cache.get(code) as CutoffRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffRule> = inner.list()
}

fun newCachedCutoffRuleStore(inner: CutoffRuleStore, ttl: Int, clock: Clock): CutoffRuleStore {
    return CachedCutoffRuleStore(
        inner = inner,
        cache = newTtlCache<CutoffRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff rules. Empty fields match all. */
data class CutoffRuleQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffRule>): MutableList<CutoffRule> {
        val matched = filter(items) { candidate: CutoffRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffRules(store: CutoffRuleStore, query: CutoffRuleQuery): MutableList<CutoffRule> {
    return query.apply(store.list())
}
