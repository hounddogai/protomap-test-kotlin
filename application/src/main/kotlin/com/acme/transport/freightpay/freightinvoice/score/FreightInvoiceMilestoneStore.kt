package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice milestones by code. */
interface FreightInvoiceMilestoneStore {
    fun find(code: String): FreightInvoiceMilestone?
    fun save(item: FreightInvoiceMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceMilestoneStore(
    var items: MutableMap<String, FreightInvoiceMilestone>,
    var order: MutableList<String>,
) : FreightInvoiceMilestoneStore {
    override fun find(code: String): FreightInvoiceMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceMilestone) {
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

    override fun list(): MutableList<FreightInvoiceMilestone> {
        val out = mutableListOf<FreightInvoiceMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceMilestoneStore(): FreightInvoiceMilestoneStore {
    return MemoryFreightInvoiceMilestoneStore(
        mutableMapOf<String, FreightInvoiceMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceMilestoneStore(
    var inner: FreightInvoiceMilestoneStore,
    var cache: TtlCache<FreightInvoiceMilestone>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceMilestoneStore {
    override fun find(code: String): FreightInvoiceMilestone? {
        val hit = (cache.get(code) as FreightInvoiceMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceMilestone> = inner.list()
}

fun newCachedFreightInvoiceMilestoneStore(inner: FreightInvoiceMilestoneStore, ttl: Int, clock: Clock): FreightInvoiceMilestoneStore {
    return CachedFreightInvoiceMilestoneStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice milestones. Empty fields match all. */
data class FreightInvoiceMilestoneQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceMilestone>): MutableList<FreightInvoiceMilestone> {
        val matched = filter(items) { candidate: FreightInvoiceMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceMilestones(store: FreightInvoiceMilestoneStore, query: FreightInvoiceMilestoneQuery): MutableList<FreightInvoiceMilestone> {
    return query.apply(store.list())
}
