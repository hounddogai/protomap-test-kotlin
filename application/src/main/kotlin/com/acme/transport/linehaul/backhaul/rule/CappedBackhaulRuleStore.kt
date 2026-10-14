package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped backhaul rules by code. */
interface CappedBackhaulRuleStore {
    fun find(code: String): CappedBackhaulRule?
    fun save(item: CappedBackhaulRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBackhaulRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBackhaulRuleStore(
    var items: MutableMap<String, CappedBackhaulRule>,
    var order: MutableList<String>,
) : CappedBackhaulRuleStore {
    override fun find(code: String): CappedBackhaulRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBackhaulRule) {
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

    override fun list(): MutableList<CappedBackhaulRule> {
        val out = mutableListOf<CappedBackhaulRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBackhaulRuleStore(): CappedBackhaulRuleStore {
    return MemoryCappedBackhaulRuleStore(mutableMapOf<String, CappedBackhaulRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBackhaulRuleStore(
    var inner: CappedBackhaulRuleStore,
    var cache: TtlCache<CappedBackhaulRule>,
    var hits: Int,
    var misses: Int,
) : CappedBackhaulRuleStore {
    override fun find(code: String): CappedBackhaulRule? {
        val hit = (cache.get(code) as CappedBackhaulRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBackhaulRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBackhaulRule> = inner.list()
}

fun newCachedCappedBackhaulRuleStore(inner: CappedBackhaulRuleStore, ttl: Int, clock: Clock): CappedBackhaulRuleStore {
    return CachedCappedBackhaulRuleStore(
        inner = inner,
        cache = newTtlCache<CappedBackhaulRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped backhaul rules. Empty fields match all. */
data class CappedBackhaulRuleQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBackhaulRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedBackhaulRule>): MutableList<CappedBackhaulRule> {
        val matched = filter(items) { candidate: CappedBackhaulRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBackhaulRules(store: CappedBackhaulRuleStore, query: CappedBackhaulRuleQuery): MutableList<CappedBackhaulRule> {
    return query.apply(store.list())
}
