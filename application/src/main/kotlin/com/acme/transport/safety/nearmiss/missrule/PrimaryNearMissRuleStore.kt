package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary near miss rules by code. */
interface PrimaryNearMissRuleStore {
    fun find(code: String): PrimaryNearMissRule?
    fun save(item: PrimaryNearMissRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNearMissRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNearMissRuleStore(
    var items: MutableMap<String, PrimaryNearMissRule>,
    var order: MutableList<String>,
) : PrimaryNearMissRuleStore {
    override fun find(code: String): PrimaryNearMissRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNearMissRule) {
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

    override fun list(): MutableList<PrimaryNearMissRule> {
        val out = mutableListOf<PrimaryNearMissRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNearMissRuleStore(): PrimaryNearMissRuleStore {
    return MemoryPrimaryNearMissRuleStore(mutableMapOf<String, PrimaryNearMissRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNearMissRuleStore(
    var inner: PrimaryNearMissRuleStore,
    var cache: TtlCache<PrimaryNearMissRule>,
    var hits: Int,
    var misses: Int,
) : PrimaryNearMissRuleStore {
    override fun find(code: String): PrimaryNearMissRule? {
        val hit = (cache.get(code) as PrimaryNearMissRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNearMissRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNearMissRule> = inner.list()
}

fun newCachedPrimaryNearMissRuleStore(inner: PrimaryNearMissRuleStore, ttl: Int, clock: Clock): PrimaryNearMissRuleStore {
    return CachedPrimaryNearMissRuleStore(
        inner = inner,
        cache = newTtlCache<PrimaryNearMissRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary near miss rules. Empty fields match all. */
data class PrimaryNearMissRuleQuery(
    var region: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNearMissRule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryNearMissRule>): MutableList<PrimaryNearMissRule> {
        val matched = filter(items) { candidate: PrimaryNearMissRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNearMissRules(store: PrimaryNearMissRuleStore, query: PrimaryNearMissRuleQuery): MutableList<PrimaryNearMissRule> {
    return query.apply(store.list())
}
