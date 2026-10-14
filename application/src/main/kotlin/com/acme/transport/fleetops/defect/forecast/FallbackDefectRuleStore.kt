package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback defect rules by code. */
interface FallbackDefectRuleStore {
    fun find(code: String): FallbackDefectRule?
    fun save(item: FallbackDefectRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDefectRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDefectRuleStore(
    var items: MutableMap<String, FallbackDefectRule>,
    var order: MutableList<String>,
) : FallbackDefectRuleStore {
    override fun find(code: String): FallbackDefectRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDefectRule) {
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

    override fun list(): MutableList<FallbackDefectRule> {
        val out = mutableListOf<FallbackDefectRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDefectRuleStore(): FallbackDefectRuleStore {
    return MemoryFallbackDefectRuleStore(mutableMapOf<String, FallbackDefectRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDefectRuleStore(
    var inner: FallbackDefectRuleStore,
    var cache: TtlCache<FallbackDefectRule>,
    var hits: Int,
    var misses: Int,
) : FallbackDefectRuleStore {
    override fun find(code: String): FallbackDefectRule? {
        val hit = (cache.get(code) as FallbackDefectRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDefectRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDefectRule> = inner.list()
}

fun newCachedFallbackDefectRuleStore(inner: FallbackDefectRuleStore, ttl: Int, clock: Clock): FallbackDefectRuleStore {
    return CachedFallbackDefectRuleStore(
        inner = inner,
        cache = newTtlCache<FallbackDefectRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback defect rules. Empty fields match all. */
data class FallbackDefectRuleQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDefectRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDefectRule>): MutableList<FallbackDefectRule> {
        val matched = filter(items) { candidate: FallbackDefectRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDefectRules(store: FallbackDefectRuleStore, query: FallbackDefectRuleQuery): MutableList<FallbackDefectRule> {
    return query.apply(store.list())
}
