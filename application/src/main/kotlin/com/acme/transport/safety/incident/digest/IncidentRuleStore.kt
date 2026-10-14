package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident rules by code. */
interface IncidentRuleStore {
    fun find(code: String): IncidentRule?
    fun save(item: IncidentRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentRuleStore(
    var items: MutableMap<String, IncidentRule>,
    var order: MutableList<String>,
) : IncidentRuleStore {
    override fun find(code: String): IncidentRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentRule) {
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

    override fun list(): MutableList<IncidentRule> {
        val out = mutableListOf<IncidentRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentRuleStore(): IncidentRuleStore {
    return MemoryIncidentRuleStore(mutableMapOf<String, IncidentRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentRuleStore(
    var inner: IncidentRuleStore,
    var cache: TtlCache<IncidentRule>,
    var hits: Int,
    var misses: Int,
) : IncidentRuleStore {
    override fun find(code: String): IncidentRule? {
        val hit = (cache.get(code) as IncidentRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentRule> = inner.list()
}

fun newCachedIncidentRuleStore(inner: IncidentRuleStore, ttl: Int, clock: Clock): IncidentRuleStore {
    return CachedIncidentRuleStore(
        inner = inner,
        cache = newTtlCache<IncidentRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident rules. Empty fields match all. */
data class IncidentRuleQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentRule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<IncidentRule>): MutableList<IncidentRule> {
        val matched = filter(items) { candidate: IncidentRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentRules(store: IncidentRuleStore, query: IncidentRuleQuery): MutableList<IncidentRule> {
    return query.apply(store.list())
}
