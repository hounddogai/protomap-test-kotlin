package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice drafts by code. */
interface FreightInvoiceDraftStore {
    fun find(code: String): FreightInvoiceDraft?
    fun save(item: FreightInvoiceDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceDraftStore(
    var items: MutableMap<String, FreightInvoiceDraft>,
    var order: MutableList<String>,
) : FreightInvoiceDraftStore {
    override fun find(code: String): FreightInvoiceDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceDraft) {
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

    override fun list(): MutableList<FreightInvoiceDraft> {
        val out = mutableListOf<FreightInvoiceDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceDraftStore(): FreightInvoiceDraftStore {
    return MemoryFreightInvoiceDraftStore(mutableMapOf<String, FreightInvoiceDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceDraftStore(
    var inner: FreightInvoiceDraftStore,
    var cache: TtlCache<FreightInvoiceDraft>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceDraftStore {
    override fun find(code: String): FreightInvoiceDraft? {
        val hit = (cache.get(code) as FreightInvoiceDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceDraft> = inner.list()
}

fun newCachedFreightInvoiceDraftStore(inner: FreightInvoiceDraftStore, ttl: Int, clock: Clock): FreightInvoiceDraftStore {
    return CachedFreightInvoiceDraftStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice drafts. Empty fields match all. */
data class FreightInvoiceDraftQuery(
    var locale: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceDraft): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceDraft>): MutableList<FreightInvoiceDraft> {
        val matched = filter(items) { candidate: FreightInvoiceDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceDrafts(store: FreightInvoiceDraftStore, query: FreightInvoiceDraftQuery): MutableList<FreightInvoiceDraft> {
    return query.apply(store.list())
}
