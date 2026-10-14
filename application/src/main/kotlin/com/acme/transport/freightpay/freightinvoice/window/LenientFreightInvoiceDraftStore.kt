package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient freight invoice drafts by code. */
interface LenientFreightInvoiceDraftStore {
    fun find(code: String): LenientFreightInvoiceDraft?
    fun save(item: LenientFreightInvoiceDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFreightInvoiceDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFreightInvoiceDraftStore(
    var items: MutableMap<String, LenientFreightInvoiceDraft>,
    var order: MutableList<String>,
) : LenientFreightInvoiceDraftStore {
    override fun find(code: String): LenientFreightInvoiceDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFreightInvoiceDraft) {
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

    override fun list(): MutableList<LenientFreightInvoiceDraft> {
        val out = mutableListOf<LenientFreightInvoiceDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFreightInvoiceDraftStore(): LenientFreightInvoiceDraftStore {
    return MemoryLenientFreightInvoiceDraftStore(
        mutableMapOf<String, LenientFreightInvoiceDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFreightInvoiceDraftStore(
    var inner: LenientFreightInvoiceDraftStore,
    var cache: TtlCache<LenientFreightInvoiceDraft>,
    var hits: Int,
    var misses: Int,
) : LenientFreightInvoiceDraftStore {
    override fun find(code: String): LenientFreightInvoiceDraft? {
        val hit = (cache.get(code) as LenientFreightInvoiceDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFreightInvoiceDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFreightInvoiceDraft> = inner.list()
}

fun newCachedLenientFreightInvoiceDraftStore(inner: LenientFreightInvoiceDraftStore, ttl: Int, clock: Clock): LenientFreightInvoiceDraftStore {
    return CachedLenientFreightInvoiceDraftStore(
        inner = inner,
        cache = newTtlCache<LenientFreightInvoiceDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient freight invoice drafts. Empty fields match all. */
data class LenientFreightInvoiceDraftQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFreightInvoiceDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LenientFreightInvoiceDraft>): MutableList<LenientFreightInvoiceDraft> {
        val matched = filter(items) { candidate: LenientFreightInvoiceDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFreightInvoiceDrafts(store: LenientFreightInvoiceDraftStore, query: LenientFreightInvoiceDraftQuery): MutableList<LenientFreightInvoiceDraft> {
    return query.apply(store.list())
}
