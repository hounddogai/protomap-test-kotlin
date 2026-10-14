package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice preferences by code. */
interface FreightInvoicePreferenceStore {
    fun find(code: String): FreightInvoicePreference?
    fun save(item: FreightInvoicePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoicePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoicePreferenceStore(
    var items: MutableMap<String, FreightInvoicePreference>,
    var order: MutableList<String>,
) : FreightInvoicePreferenceStore {
    override fun find(code: String): FreightInvoicePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoicePreference) {
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

    override fun list(): MutableList<FreightInvoicePreference> {
        val out = mutableListOf<FreightInvoicePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoicePreferenceStore(): FreightInvoicePreferenceStore {
    return MemoryFreightInvoicePreferenceStore(
        mutableMapOf<String, FreightInvoicePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoicePreferenceStore(
    var inner: FreightInvoicePreferenceStore,
    var cache: TtlCache<FreightInvoicePreference>,
    var hits: Int,
    var misses: Int,
) : FreightInvoicePreferenceStore {
    override fun find(code: String): FreightInvoicePreference? {
        val hit = (cache.get(code) as FreightInvoicePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoicePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoicePreference> = inner.list()
}

fun newCachedFreightInvoicePreferenceStore(inner: FreightInvoicePreferenceStore, ttl: Int, clock: Clock): FreightInvoicePreferenceStore {
    return CachedFreightInvoicePreferenceStore(
        inner = inner,
        cache = newTtlCache<FreightInvoicePreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice preferences. Empty fields match all. */
data class FreightInvoicePreferenceQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoicePreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoicePreference>): MutableList<FreightInvoicePreference> {
        val matched = filter(items) { candidate: FreightInvoicePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoicePreferences(store: FreightInvoicePreferenceStore, query: FreightInvoicePreferenceQuery): MutableList<FreightInvoicePreference> {
    return query.apply(store.list())
}
