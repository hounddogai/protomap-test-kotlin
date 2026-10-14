package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation rules by code. */
interface SortationRuleStore {
    fun find(code: String): SortationRule?
    fun save(item: SortationRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationRuleStore(
    var items: MutableMap<String, SortationRule>,
    var order: MutableList<String>,
) : SortationRuleStore {
    override fun find(code: String): SortationRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationRule) {
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

    override fun list(): MutableList<SortationRule> {
        val out = mutableListOf<SortationRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationRuleStore(): SortationRuleStore {
    return MemorySortationRuleStore(mutableMapOf<String, SortationRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationRuleStore(
    var inner: SortationRuleStore,
    var cache: TtlCache<SortationRule>,
    var hits: Int,
    var misses: Int,
) : SortationRuleStore {
    override fun find(code: String): SortationRule? {
        val hit = (cache.get(code) as SortationRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationRule> = inner.list()
}

fun newCachedSortationRuleStore(inner: SortationRuleStore, ttl: Int, clock: Clock): SortationRuleStore {
    return CachedSortationRuleStore(
        inner = inner,
        cache = newTtlCache<SortationRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation rules. Empty fields match all. */
data class SortationRuleQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationRule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<SortationRule>): MutableList<SortationRule> {
        val matched = filter(items) { candidate: SortationRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationRules(store: SortationRuleStore, query: SortationRuleQuery): MutableList<SortationRule> {
    return query.apply(store.list())
}
