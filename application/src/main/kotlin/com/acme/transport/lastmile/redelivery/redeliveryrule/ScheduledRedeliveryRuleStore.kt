package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled redelivery rules by code. */
interface ScheduledRedeliveryRuleStore {
    fun find(code: String): ScheduledRedeliveryRule?
    fun save(item: ScheduledRedeliveryRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRedeliveryRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRedeliveryRuleStore(
    var items: MutableMap<String, ScheduledRedeliveryRule>,
    var order: MutableList<String>,
) : ScheduledRedeliveryRuleStore {
    override fun find(code: String): ScheduledRedeliveryRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRedeliveryRule) {
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

    override fun list(): MutableList<ScheduledRedeliveryRule> {
        val out = mutableListOf<ScheduledRedeliveryRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRedeliveryRuleStore(): ScheduledRedeliveryRuleStore {
    return MemoryScheduledRedeliveryRuleStore(
        mutableMapOf<String, ScheduledRedeliveryRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRedeliveryRuleStore(
    var inner: ScheduledRedeliveryRuleStore,
    var cache: TtlCache<ScheduledRedeliveryRule>,
    var hits: Int,
    var misses: Int,
) : ScheduledRedeliveryRuleStore {
    override fun find(code: String): ScheduledRedeliveryRule? {
        val hit = (cache.get(code) as ScheduledRedeliveryRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRedeliveryRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRedeliveryRule> = inner.list()
}

fun newCachedScheduledRedeliveryRuleStore(inner: ScheduledRedeliveryRuleStore, ttl: Int, clock: Clock): ScheduledRedeliveryRuleStore {
    return CachedScheduledRedeliveryRuleStore(
        inner = inner,
        cache = newTtlCache<ScheduledRedeliveryRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled redelivery rules. Empty fields match all. */
data class ScheduledRedeliveryRuleQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRedeliveryRule): Boolean {
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

    fun apply(items: MutableList<ScheduledRedeliveryRule>): MutableList<ScheduledRedeliveryRule> {
        val matched = filter(items) { candidate: ScheduledRedeliveryRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRedeliveryRules(store: ScheduledRedeliveryRuleStore, query: ScheduledRedeliveryRuleQuery): MutableList<ScheduledRedeliveryRule> {
    return query.apply(store.list())
}
