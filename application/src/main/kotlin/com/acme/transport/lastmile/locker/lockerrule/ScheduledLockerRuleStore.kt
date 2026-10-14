package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled locker rules by code. */
interface ScheduledLockerRuleStore {
    fun find(code: String): ScheduledLockerRule?
    fun save(item: ScheduledLockerRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLockerRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLockerRuleStore(
    var items: MutableMap<String, ScheduledLockerRule>,
    var order: MutableList<String>,
) : ScheduledLockerRuleStore {
    override fun find(code: String): ScheduledLockerRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLockerRule) {
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

    override fun list(): MutableList<ScheduledLockerRule> {
        val out = mutableListOf<ScheduledLockerRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLockerRuleStore(): ScheduledLockerRuleStore {
    return MemoryScheduledLockerRuleStore(mutableMapOf<String, ScheduledLockerRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLockerRuleStore(
    var inner: ScheduledLockerRuleStore,
    var cache: TtlCache<ScheduledLockerRule>,
    var hits: Int,
    var misses: Int,
) : ScheduledLockerRuleStore {
    override fun find(code: String): ScheduledLockerRule? {
        val hit = (cache.get(code) as ScheduledLockerRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLockerRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLockerRule> = inner.list()
}

fun newCachedScheduledLockerRuleStore(inner: ScheduledLockerRuleStore, ttl: Int, clock: Clock): ScheduledLockerRuleStore {
    return CachedScheduledLockerRuleStore(
        inner = inner,
        cache = newTtlCache<ScheduledLockerRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled locker rules. Empty fields match all. */
data class ScheduledLockerRuleQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLockerRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLockerRule>): MutableList<ScheduledLockerRule> {
        val matched = filter(items) { candidate: ScheduledLockerRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLockerRules(store: ScheduledLockerRuleStore, query: ScheduledLockerRuleQuery): MutableList<ScheduledLockerRule> {
    return query.apply(store.list())
}
