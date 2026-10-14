package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy freight invoice rules by code. */
interface LegacyFreightInvoiceRuleStore {
    fun find(code: String): LegacyFreightInvoiceRule?
    fun save(item: LegacyFreightInvoiceRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyFreightInvoiceRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyFreightInvoiceRuleStore(
    var items: MutableMap<String, LegacyFreightInvoiceRule>,
    var order: MutableList<String>,
) : LegacyFreightInvoiceRuleStore {
    override fun find(code: String): LegacyFreightInvoiceRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyFreightInvoiceRule) {
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

    override fun list(): MutableList<LegacyFreightInvoiceRule> {
        val out = mutableListOf<LegacyFreightInvoiceRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyFreightInvoiceRuleStore(): LegacyFreightInvoiceRuleStore {
    return MemoryLegacyFreightInvoiceRuleStore(
        mutableMapOf<String, LegacyFreightInvoiceRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyFreightInvoiceRuleStore(
    var inner: LegacyFreightInvoiceRuleStore,
    var cache: TtlCache<LegacyFreightInvoiceRule>,
    var hits: Int,
    var misses: Int,
) : LegacyFreightInvoiceRuleStore {
    override fun find(code: String): LegacyFreightInvoiceRule? {
        val hit = (cache.get(code) as LegacyFreightInvoiceRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyFreightInvoiceRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyFreightInvoiceRule> = inner.list()
}

fun newCachedLegacyFreightInvoiceRuleStore(inner: LegacyFreightInvoiceRuleStore, ttl: Int, clock: Clock): LegacyFreightInvoiceRuleStore {
    return CachedLegacyFreightInvoiceRuleStore(
        inner = inner,
        cache = newTtlCache<LegacyFreightInvoiceRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy freight invoice rules. Empty fields match all. */
data class LegacyFreightInvoiceRuleQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyFreightInvoiceRule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LegacyFreightInvoiceRule>): MutableList<LegacyFreightInvoiceRule> {
        val matched = filter(items) { candidate: LegacyFreightInvoiceRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyFreightInvoiceRules(store: LegacyFreightInvoiceRuleStore, query: LegacyFreightInvoiceRuleQuery): MutableList<LegacyFreightInvoiceRule> {
    return query.apply(store.list())
}
