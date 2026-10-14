package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door rules by code. */
interface DockDoorRuleStore {
    fun find(code: String): DockDoorRule?
    fun save(item: DockDoorRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorRuleStore(
    var items: MutableMap<String, DockDoorRule>,
    var order: MutableList<String>,
) : DockDoorRuleStore {
    override fun find(code: String): DockDoorRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorRule) {
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

    override fun list(): MutableList<DockDoorRule> {
        val out = mutableListOf<DockDoorRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorRuleStore(): DockDoorRuleStore {
    return MemoryDockDoorRuleStore(mutableMapOf<String, DockDoorRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorRuleStore(
    var inner: DockDoorRuleStore,
    var cache: TtlCache<DockDoorRule>,
    var hits: Int,
    var misses: Int,
) : DockDoorRuleStore {
    override fun find(code: String): DockDoorRule? {
        val hit = (cache.get(code) as DockDoorRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorRule> = inner.list()
}

fun newCachedDockDoorRuleStore(inner: DockDoorRuleStore, ttl: Int, clock: Clock): DockDoorRuleStore {
    return CachedDockDoorRuleStore(
        inner = inner,
        cache = newTtlCache<DockDoorRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door rules. Empty fields match all. */
data class DockDoorRuleQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorRule>): MutableList<DockDoorRule> {
        val matched = filter(items) { candidate: DockDoorRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorRules(store: DockDoorRuleStore, query: DockDoorRuleQuery): MutableList<DockDoorRule> {
    return query.apply(store.list())
}
