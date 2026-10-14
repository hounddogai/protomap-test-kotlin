package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary freight invoice templates by code. */
interface PrimaryFreightInvoiceTemplateStore {
    fun find(code: String): PrimaryFreightInvoiceTemplate?
    fun save(item: PrimaryFreightInvoiceTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFreightInvoiceTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFreightInvoiceTemplateStore(
    var items: MutableMap<String, PrimaryFreightInvoiceTemplate>,
    var order: MutableList<String>,
) : PrimaryFreightInvoiceTemplateStore {
    override fun find(code: String): PrimaryFreightInvoiceTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFreightInvoiceTemplate) {
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

    override fun list(): MutableList<PrimaryFreightInvoiceTemplate> {
        val out = mutableListOf<PrimaryFreightInvoiceTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFreightInvoiceTemplateStore(): PrimaryFreightInvoiceTemplateStore {
    return MemoryPrimaryFreightInvoiceTemplateStore(
        mutableMapOf<String, PrimaryFreightInvoiceTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFreightInvoiceTemplateStore(
    var inner: PrimaryFreightInvoiceTemplateStore,
    var cache: TtlCache<PrimaryFreightInvoiceTemplate>,
    var hits: Int,
    var misses: Int,
) : PrimaryFreightInvoiceTemplateStore {
    override fun find(code: String): PrimaryFreightInvoiceTemplate? {
        val hit = (cache.get(code) as PrimaryFreightInvoiceTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFreightInvoiceTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFreightInvoiceTemplate> = inner.list()
}

fun newCachedPrimaryFreightInvoiceTemplateStore(inner: PrimaryFreightInvoiceTemplateStore, ttl: Int, clock: Clock): PrimaryFreightInvoiceTemplateStore {
    return CachedPrimaryFreightInvoiceTemplateStore(
        inner = inner,
        cache = newTtlCache<PrimaryFreightInvoiceTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary freight invoice templates. Empty fields match all. */
data class PrimaryFreightInvoiceTemplateQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFreightInvoiceTemplate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFreightInvoiceTemplate>): MutableList<PrimaryFreightInvoiceTemplate> {
        val matched = filter(items) { candidate: PrimaryFreightInvoiceTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFreightInvoiceTemplates(store: PrimaryFreightInvoiceTemplateStore, query: PrimaryFreightInvoiceTemplateQuery): MutableList<PrimaryFreightInvoiceTemplate> {
    return query.apply(store.list())
}
