package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default sortation rules by code. */
interface DefaultSortationRuleStore {
    fun find(code: String): DefaultSortationRule?
    fun save(item: DefaultSortationRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultSortationRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultSortationRuleStore(
    var items: MutableMap<String, DefaultSortationRule>,
    var order: MutableList<String>,
) : DefaultSortationRuleStore {
    override fun find(code: String): DefaultSortationRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultSortationRule) {
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

    override fun list(): MutableList<DefaultSortationRule> {
        val out = mutableListOf<DefaultSortationRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultSortationRuleStore(): DefaultSortationRuleStore {
    return MemoryDefaultSortationRuleStore(
        mutableMapOf<String, DefaultSortationRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultSortationRuleStore(
    var inner: DefaultSortationRuleStore,
    var cache: TtlCache<DefaultSortationRule>,
    var hits: Int,
    var misses: Int,
) : DefaultSortationRuleStore {
    override fun find(code: String): DefaultSortationRule? {
        val hit = (cache.get(code) as DefaultSortationRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultSortationRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultSortationRule> = inner.list()
}

fun newCachedDefaultSortationRuleStore(inner: DefaultSortationRuleStore, ttl: Int, clock: Clock): DefaultSortationRuleStore {
    return CachedDefaultSortationRuleStore(
        inner = inner,
        cache = newTtlCache<DefaultSortationRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default sortation rules. Empty fields match all. */
data class DefaultSortationRuleQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultSortationRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultSortationRule>): MutableList<DefaultSortationRule> {
        val matched = filter(items) { candidate: DefaultSortationRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultSortationRules(store: DefaultSortationRuleStore, query: DefaultSortationRuleQuery): MutableList<DefaultSortationRule> {
    return query.apply(store.list())
}
