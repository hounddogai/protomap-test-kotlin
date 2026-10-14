package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling trailer yard rules by code. */
interface RollingTrailerYardRuleStore {
    fun find(code: String): RollingTrailerYardRule?
    fun save(item: RollingTrailerYardRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerYardRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerYardRuleStore(
    var items: MutableMap<String, RollingTrailerYardRule>,
    var order: MutableList<String>,
) : RollingTrailerYardRuleStore {
    override fun find(code: String): RollingTrailerYardRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerYardRule) {
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

    override fun list(): MutableList<RollingTrailerYardRule> {
        val out = mutableListOf<RollingTrailerYardRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerYardRuleStore(): RollingTrailerYardRuleStore {
    return MemoryRollingTrailerYardRuleStore(
        mutableMapOf<String, RollingTrailerYardRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerYardRuleStore(
    var inner: RollingTrailerYardRuleStore,
    var cache: TtlCache<RollingTrailerYardRule>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerYardRuleStore {
    override fun find(code: String): RollingTrailerYardRule? {
        val hit = (cache.get(code) as RollingTrailerYardRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerYardRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerYardRule> = inner.list()
}

fun newCachedRollingTrailerYardRuleStore(inner: RollingTrailerYardRuleStore, ttl: Int, clock: Clock): RollingTrailerYardRuleStore {
    return CachedRollingTrailerYardRuleStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerYardRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer yard rules. Empty fields match all. */
data class RollingTrailerYardRuleQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerYardRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTrailerYardRule>): MutableList<RollingTrailerYardRule> {
        val matched = filter(items) { candidate: RollingTrailerYardRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerYardRules(store: RollingTrailerYardRuleStore, query: RollingTrailerYardRuleQuery): MutableList<RollingTrailerYardRule> {
    return query.apply(store.list())
}
