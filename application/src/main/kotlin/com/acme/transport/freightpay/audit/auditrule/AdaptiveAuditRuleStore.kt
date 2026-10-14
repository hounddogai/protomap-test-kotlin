package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive audit rules by code. */
interface AdaptiveAuditRuleStore {
    fun find(code: String): AdaptiveAuditRule?
    fun save(item: AdaptiveAuditRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAuditRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAuditRuleStore(
    var items: MutableMap<String, AdaptiveAuditRule>,
    var order: MutableList<String>,
) : AdaptiveAuditRuleStore {
    override fun find(code: String): AdaptiveAuditRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAuditRule) {
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

    override fun list(): MutableList<AdaptiveAuditRule> {
        val out = mutableListOf<AdaptiveAuditRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAuditRuleStore(): AdaptiveAuditRuleStore {
    return MemoryAdaptiveAuditRuleStore(mutableMapOf<String, AdaptiveAuditRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAuditRuleStore(
    var inner: AdaptiveAuditRuleStore,
    var cache: TtlCache<AdaptiveAuditRule>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAuditRuleStore {
    override fun find(code: String): AdaptiveAuditRule? {
        val hit = (cache.get(code) as AdaptiveAuditRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAuditRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAuditRule> = inner.list()
}

fun newCachedAdaptiveAuditRuleStore(inner: AdaptiveAuditRuleStore, ttl: Int, clock: Clock): AdaptiveAuditRuleStore {
    return CachedAdaptiveAuditRuleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAuditRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive audit rules. Empty fields match all. */
data class AdaptiveAuditRuleQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAuditRule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAuditRule>): MutableList<AdaptiveAuditRule> {
        val matched = filter(items) { candidate: AdaptiveAuditRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAuditRules(store: AdaptiveAuditRuleStore, query: AdaptiveAuditRuleQuery): MutableList<AdaptiveAuditRule> {
    return query.apply(store.list())
}
