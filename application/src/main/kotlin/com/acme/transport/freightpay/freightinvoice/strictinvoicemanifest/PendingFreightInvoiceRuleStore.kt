package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending freight invoice rules by code. */
interface PendingFreightInvoiceRuleStore {
    fun find(code: String): PendingFreightInvoiceRule?
    fun save(item: PendingFreightInvoiceRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFreightInvoiceRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFreightInvoiceRuleStore(
    var items: MutableMap<String, PendingFreightInvoiceRule>,
    var order: MutableList<String>,
) : PendingFreightInvoiceRuleStore {
    override fun find(code: String): PendingFreightInvoiceRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFreightInvoiceRule) {
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

    override fun list(): MutableList<PendingFreightInvoiceRule> {
        val out = mutableListOf<PendingFreightInvoiceRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFreightInvoiceRuleStore(): PendingFreightInvoiceRuleStore {
    return MemoryPendingFreightInvoiceRuleStore(
        mutableMapOf<String, PendingFreightInvoiceRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFreightInvoiceRuleStore(
    var inner: PendingFreightInvoiceRuleStore,
    var cache: TtlCache<PendingFreightInvoiceRule>,
    var hits: Int,
    var misses: Int,
) : PendingFreightInvoiceRuleStore {
    override fun find(code: String): PendingFreightInvoiceRule? {
        val hit = (cache.get(code) as PendingFreightInvoiceRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFreightInvoiceRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFreightInvoiceRule> = inner.list()
}

fun newCachedPendingFreightInvoiceRuleStore(inner: PendingFreightInvoiceRuleStore, ttl: Int, clock: Clock): PendingFreightInvoiceRuleStore {
    return CachedPendingFreightInvoiceRuleStore(
        inner = inner,
        cache = newTtlCache<PendingFreightInvoiceRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending freight invoice rules. Empty fields match all. */
data class PendingFreightInvoiceRuleQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFreightInvoiceRule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingFreightInvoiceRule>): MutableList<PendingFreightInvoiceRule> {
        val matched = filter(items) { candidate: PendingFreightInvoiceRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFreightInvoiceRules(store: PendingFreightInvoiceRuleStore, query: PendingFreightInvoiceRuleQuery): MutableList<PendingFreightInvoiceRule> {
    return query.apply(store.list())
}
