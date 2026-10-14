package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary drop rules by code. */
interface PrimaryDropRuleStore {
    fun find(code: String): PrimaryDropRule?
    fun save(item: PrimaryDropRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDropRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDropRuleStore(
    var items: MutableMap<String, PrimaryDropRule>,
    var order: MutableList<String>,
) : PrimaryDropRuleStore {
    override fun find(code: String): PrimaryDropRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDropRule) {
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

    override fun list(): MutableList<PrimaryDropRule> {
        val out = mutableListOf<PrimaryDropRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDropRuleStore(): PrimaryDropRuleStore {
    return MemoryPrimaryDropRuleStore(mutableMapOf<String, PrimaryDropRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDropRuleStore(
    var inner: PrimaryDropRuleStore,
    var cache: TtlCache<PrimaryDropRule>,
    var hits: Int,
    var misses: Int,
) : PrimaryDropRuleStore {
    override fun find(code: String): PrimaryDropRule? {
        val hit = (cache.get(code) as PrimaryDropRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDropRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDropRule> = inner.list()
}

fun newCachedPrimaryDropRuleStore(inner: PrimaryDropRuleStore, ttl: Int, clock: Clock): PrimaryDropRuleStore {
    return CachedPrimaryDropRuleStore(
        inner = inner,
        cache = newTtlCache<PrimaryDropRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary drop rules. Empty fields match all. */
data class PrimaryDropRuleQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDropRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDropRule>): MutableList<PrimaryDropRule> {
        val matched = filter(items) { candidate: PrimaryDropRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDropRules(store: PrimaryDropRuleStore, query: PrimaryDropRuleQuery): MutableList<PrimaryDropRule> {
    return query.apply(store.list())
}
