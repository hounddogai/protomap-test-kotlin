package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback freight invoice notices by code. */
interface FallbackFreightInvoiceNoticeStore {
    fun find(code: String): FallbackFreightInvoiceNotice?
    fun save(item: FallbackFreightInvoiceNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackFreightInvoiceNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackFreightInvoiceNoticeStore(
    var items: MutableMap<String, FallbackFreightInvoiceNotice>,
    var order: MutableList<String>,
) : FallbackFreightInvoiceNoticeStore {
    override fun find(code: String): FallbackFreightInvoiceNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackFreightInvoiceNotice) {
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

    override fun list(): MutableList<FallbackFreightInvoiceNotice> {
        val out = mutableListOf<FallbackFreightInvoiceNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackFreightInvoiceNoticeStore(): FallbackFreightInvoiceNoticeStore {
    return MemoryFallbackFreightInvoiceNoticeStore(
        mutableMapOf<String, FallbackFreightInvoiceNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackFreightInvoiceNoticeStore(
    var inner: FallbackFreightInvoiceNoticeStore,
    var cache: TtlCache<FallbackFreightInvoiceNotice>,
    var hits: Int,
    var misses: Int,
) : FallbackFreightInvoiceNoticeStore {
    override fun find(code: String): FallbackFreightInvoiceNotice? {
        val hit = (cache.get(code) as FallbackFreightInvoiceNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackFreightInvoiceNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackFreightInvoiceNotice> = inner.list()
}

fun newCachedFallbackFreightInvoiceNoticeStore(inner: FallbackFreightInvoiceNoticeStore, ttl: Int, clock: Clock): FallbackFreightInvoiceNoticeStore {
    return CachedFallbackFreightInvoiceNoticeStore(
        inner = inner,
        cache = newTtlCache<FallbackFreightInvoiceNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback freight invoice notices. Empty fields match all. */
data class FallbackFreightInvoiceNoticeQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackFreightInvoiceNotice): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<FallbackFreightInvoiceNotice>): MutableList<FallbackFreightInvoiceNotice> {
        val matched = filter(items) { candidate: FallbackFreightInvoiceNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackFreightInvoiceNotices(store: FallbackFreightInvoiceNoticeStore, query: FallbackFreightInvoiceNoticeQuery): MutableList<FallbackFreightInvoiceNotice> {
    return query.apply(store.list())
}
