package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient freight invoice targets by code. */
interface LenientFreightInvoiceTargetStore {
    fun find(code: String): LenientFreightInvoiceTarget?
    fun save(item: LenientFreightInvoiceTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFreightInvoiceTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFreightInvoiceTargetStore(
    var items: MutableMap<String, LenientFreightInvoiceTarget>,
    var order: MutableList<String>,
) : LenientFreightInvoiceTargetStore {
    override fun find(code: String): LenientFreightInvoiceTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFreightInvoiceTarget) {
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

    override fun list(): MutableList<LenientFreightInvoiceTarget> {
        val out = mutableListOf<LenientFreightInvoiceTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFreightInvoiceTargetStore(): LenientFreightInvoiceTargetStore {
    return MemoryLenientFreightInvoiceTargetStore(
        mutableMapOf<String, LenientFreightInvoiceTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFreightInvoiceTargetStore(
    var inner: LenientFreightInvoiceTargetStore,
    var cache: TtlCache<LenientFreightInvoiceTarget>,
    var hits: Int,
    var misses: Int,
) : LenientFreightInvoiceTargetStore {
    override fun find(code: String): LenientFreightInvoiceTarget? {
        val hit = (cache.get(code) as LenientFreightInvoiceTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFreightInvoiceTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFreightInvoiceTarget> = inner.list()
}

fun newCachedLenientFreightInvoiceTargetStore(inner: LenientFreightInvoiceTargetStore, ttl: Int, clock: Clock): LenientFreightInvoiceTargetStore {
    return CachedLenientFreightInvoiceTargetStore(
        inner = inner,
        cache = newTtlCache<LenientFreightInvoiceTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient freight invoice targets. Empty fields match all. */
data class LenientFreightInvoiceTargetQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFreightInvoiceTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFreightInvoiceTarget>): MutableList<LenientFreightInvoiceTarget> {
        val matched = filter(items) { candidate: LenientFreightInvoiceTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFreightInvoiceTargets(store: LenientFreightInvoiceTargetStore, query: LenientFreightInvoiceTargetQuery): MutableList<LenientFreightInvoiceTarget> {
    return query.apply(store.list())
}
