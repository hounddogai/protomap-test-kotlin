package com.acme.transport.network.zone.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone rules by code. */
interface ZoneRuleStore {
    fun find(code: String): ZoneRule?
    fun save(item: ZoneRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneRuleStore(
    var items: MutableMap<String, ZoneRule>,
    var order: MutableList<String>,
) : ZoneRuleStore {
    override fun find(code: String): ZoneRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneRule) {
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

    override fun list(): MutableList<ZoneRule> {
        val out = mutableListOf<ZoneRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneRuleStore(): ZoneRuleStore {
    return MemoryZoneRuleStore(mutableMapOf<String, ZoneRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneRuleStore(
    var inner: ZoneRuleStore,
    var cache: TtlCache<ZoneRule>,
    var hits: Int,
    var misses: Int,
) : ZoneRuleStore {
    override fun find(code: String): ZoneRule? {
        val hit = (cache.get(code) as ZoneRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneRule> = inner.list()
}

fun newCachedZoneRuleStore(inner: ZoneRuleStore, ttl: Int, clock: Clock): ZoneRuleStore {
    return CachedZoneRuleStore(
        inner = inner,
        cache = newTtlCache<ZoneRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone rules. Empty fields match all. */
data class ZoneRuleQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneRule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneRule>): MutableList<ZoneRule> {
        val matched = filter(items) { candidate: ZoneRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneRules(store: ZoneRuleStore, query: ZoneRuleQuery): MutableList<ZoneRule> = query.apply(store.list())
