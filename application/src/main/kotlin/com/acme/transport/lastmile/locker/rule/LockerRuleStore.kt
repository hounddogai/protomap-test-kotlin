package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker rules by code. */
interface LockerRuleStore {
    fun find(code: String): LockerRule?
    fun save(item: LockerRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerRuleStore(
    var items: MutableMap<String, LockerRule>,
    var order: MutableList<String>,
) : LockerRuleStore {
    override fun find(code: String): LockerRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerRule) {
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

    override fun list(): MutableList<LockerRule> {
        val out = mutableListOf<LockerRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerRuleStore(): LockerRuleStore {
    return MemoryLockerRuleStore(mutableMapOf<String, LockerRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerRuleStore(
    var inner: LockerRuleStore,
    var cache: TtlCache<LockerRule>,
    var hits: Int,
    var misses: Int,
) : LockerRuleStore {
    override fun find(code: String): LockerRule? {
        val hit = (cache.get(code) as LockerRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerRule> = inner.list()
}

fun newCachedLockerRuleStore(inner: LockerRuleStore, ttl: Int, clock: Clock): LockerRuleStore {
    return CachedLockerRuleStore(
        inner = inner,
        cache = newTtlCache<LockerRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker rules. Empty fields match all. */
data class LockerRuleQuery(
    var unit: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerRule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerRule>): MutableList<LockerRule> {
        val matched = filter(items) { candidate: LockerRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerRules(store: LockerRuleStore, query: LockerRuleQuery): MutableList<LockerRule> {
    return query.apply(store.list())
}
