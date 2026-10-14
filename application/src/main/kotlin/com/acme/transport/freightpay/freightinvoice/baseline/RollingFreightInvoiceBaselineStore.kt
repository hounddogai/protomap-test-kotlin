package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling freight invoice baselines by code. */
interface RollingFreightInvoiceBaselineStore {
    fun find(code: String): RollingFreightInvoiceBaseline?
    fun save(item: RollingFreightInvoiceBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFreightInvoiceBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFreightInvoiceBaselineStore(
    var items: MutableMap<String, RollingFreightInvoiceBaseline>,
    var order: MutableList<String>,
) : RollingFreightInvoiceBaselineStore {
    override fun find(code: String): RollingFreightInvoiceBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFreightInvoiceBaseline) {
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

    override fun list(): MutableList<RollingFreightInvoiceBaseline> {
        val out = mutableListOf<RollingFreightInvoiceBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFreightInvoiceBaselineStore(): RollingFreightInvoiceBaselineStore {
    return MemoryRollingFreightInvoiceBaselineStore(
        mutableMapOf<String, RollingFreightInvoiceBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFreightInvoiceBaselineStore(
    var inner: RollingFreightInvoiceBaselineStore,
    var cache: TtlCache<RollingFreightInvoiceBaseline>,
    var hits: Int,
    var misses: Int,
) : RollingFreightInvoiceBaselineStore {
    override fun find(code: String): RollingFreightInvoiceBaseline? {
        val hit = (cache.get(code) as RollingFreightInvoiceBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFreightInvoiceBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFreightInvoiceBaseline> = inner.list()
}

fun newCachedRollingFreightInvoiceBaselineStore(inner: RollingFreightInvoiceBaselineStore, ttl: Int, clock: Clock): RollingFreightInvoiceBaselineStore {
    return CachedRollingFreightInvoiceBaselineStore(
        inner = inner,
        cache = newTtlCache<RollingFreightInvoiceBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling freight invoice baselines. Empty fields match all. */
data class RollingFreightInvoiceBaselineQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFreightInvoiceBaseline): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingFreightInvoiceBaseline>): MutableList<RollingFreightInvoiceBaseline> {
        val matched = filter(items) { candidate: RollingFreightInvoiceBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFreightInvoiceBaselines(store: RollingFreightInvoiceBaselineStore, query: RollingFreightInvoiceBaselineQuery): MutableList<RollingFreightInvoiceBaseline> {
    return query.apply(store.list())
}
