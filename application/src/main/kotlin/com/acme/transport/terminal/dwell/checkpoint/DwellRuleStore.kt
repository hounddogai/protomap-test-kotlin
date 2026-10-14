package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell rules by code. */
interface DwellRuleStore {
    fun find(code: String): DwellRule?
    fun save(item: DwellRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellRuleStore(
    var items: MutableMap<String, DwellRule>,
    var order: MutableList<String>,
) : DwellRuleStore {
    override fun find(code: String): DwellRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellRule) {
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

    override fun list(): MutableList<DwellRule> {
        val out = mutableListOf<DwellRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellRuleStore(): DwellRuleStore {
    return MemoryDwellRuleStore(mutableMapOf<String, DwellRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellRuleStore(
    var inner: DwellRuleStore,
    var cache: TtlCache<DwellRule>,
    var hits: Int,
    var misses: Int,
) : DwellRuleStore {
    override fun find(code: String): DwellRule? {
        val hit = (cache.get(code) as DwellRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellRule> = inner.list()
}

fun newCachedDwellRuleStore(inner: DwellRuleStore, ttl: Int, clock: Clock): DwellRuleStore {
    return CachedDwellRuleStore(
        inner = inner,
        cache = newTtlCache<DwellRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell rules. Empty fields match all. */
data class DwellRuleQuery(
    var channel: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellRule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<DwellRule>): MutableList<DwellRule> {
        val matched = filter(items) { candidate: DwellRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellRules(store: DwellRuleStore, query: DwellRuleQuery): MutableList<DwellRule> {
    return query.apply(store.list())
}
