package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock rules by code. */
interface CrossDockRuleStore {
    fun find(code: String): CrossDockRule?
    fun save(item: CrossDockRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockRuleStore(
    var items: MutableMap<String, CrossDockRule>,
    var order: MutableList<String>,
) : CrossDockRuleStore {
    override fun find(code: String): CrossDockRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockRule) {
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

    override fun list(): MutableList<CrossDockRule> {
        val out = mutableListOf<CrossDockRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockRuleStore(): CrossDockRuleStore {
    return MemoryCrossDockRuleStore(mutableMapOf<String, CrossDockRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockRuleStore(
    var inner: CrossDockRuleStore,
    var cache: TtlCache<CrossDockRule>,
    var hits: Int,
    var misses: Int,
) : CrossDockRuleStore {
    override fun find(code: String): CrossDockRule? {
        val hit = (cache.get(code) as CrossDockRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockRule> = inner.list()
}

fun newCachedCrossDockRuleStore(inner: CrossDockRuleStore, ttl: Int, clock: Clock): CrossDockRuleStore {
    return CachedCrossDockRuleStore(
        inner = inner,
        cache = newTtlCache<CrossDockRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock rules. Empty fields match all. */
data class CrossDockRuleQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CrossDockRule>): MutableList<CrossDockRule> {
        val matched = filter(items) { candidate: CrossDockRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockRules(store: CrossDockRuleStore, query: CrossDockRuleQuery): MutableList<CrossDockRule> {
    return query.apply(store.list())
}
