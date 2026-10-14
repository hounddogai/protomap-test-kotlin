package com.acme.transport.freightpay.audit.batch

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit rules by code. */
interface AuditRuleStore {
    fun find(code: String): AuditRule?
    fun save(item: AuditRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditRuleStore(
    var items: MutableMap<String, AuditRule>,
    var order: MutableList<String>,
) : AuditRuleStore {
    override fun find(code: String): AuditRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditRule) {
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

    override fun list(): MutableList<AuditRule> {
        val out = mutableListOf<AuditRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditRuleStore(): AuditRuleStore {
    return MemoryAuditRuleStore(mutableMapOf<String, AuditRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditRuleStore(
    var inner: AuditRuleStore,
    var cache: TtlCache<AuditRule>,
    var hits: Int,
    var misses: Int,
) : AuditRuleStore {
    override fun find(code: String): AuditRule? {
        val hit = (cache.get(code) as AuditRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditRule> = inner.list()
}

fun newCachedAuditRuleStore(inner: AuditRuleStore, ttl: Int, clock: Clock): AuditRuleStore {
    return CachedAuditRuleStore(
        inner = inner,
        cache = newTtlCache<AuditRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit rules. Empty fields match all. */
data class AuditRuleQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditRule>): MutableList<AuditRule> {
        val matched = filter(items) { candidate: AuditRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditRules(store: AuditRuleStore, query: AuditRuleQuery): MutableList<AuditRule> {
    return query.apply(store.list())
}
