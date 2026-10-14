package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal freight invoice proposals by code. */
interface SeasonalFreightInvoiceProposalStore {
    fun find(code: String): SeasonalFreightInvoiceProposal?
    fun save(item: SeasonalFreightInvoiceProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFreightInvoiceProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFreightInvoiceProposalStore(
    var items: MutableMap<String, SeasonalFreightInvoiceProposal>,
    var order: MutableList<String>,
) : SeasonalFreightInvoiceProposalStore {
    override fun find(code: String): SeasonalFreightInvoiceProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFreightInvoiceProposal) {
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

    override fun list(): MutableList<SeasonalFreightInvoiceProposal> {
        val out = mutableListOf<SeasonalFreightInvoiceProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFreightInvoiceProposalStore(): SeasonalFreightInvoiceProposalStore {
    return MemorySeasonalFreightInvoiceProposalStore(
        mutableMapOf<String, SeasonalFreightInvoiceProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFreightInvoiceProposalStore(
    var inner: SeasonalFreightInvoiceProposalStore,
    var cache: TtlCache<SeasonalFreightInvoiceProposal>,
    var hits: Int,
    var misses: Int,
) : SeasonalFreightInvoiceProposalStore {
    override fun find(code: String): SeasonalFreightInvoiceProposal? {
        val hit = (cache.get(code) as SeasonalFreightInvoiceProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFreightInvoiceProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFreightInvoiceProposal> = inner.list()
}

fun newCachedSeasonalFreightInvoiceProposalStore(inner: SeasonalFreightInvoiceProposalStore, ttl: Int, clock: Clock): SeasonalFreightInvoiceProposalStore {
    return CachedSeasonalFreightInvoiceProposalStore(
        inner = inner,
        cache = newTtlCache<SeasonalFreightInvoiceProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal freight invoice proposals. Empty fields match all. */
data class SeasonalFreightInvoiceProposalQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFreightInvoiceProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFreightInvoiceProposal>): MutableList<SeasonalFreightInvoiceProposal> {
        val matched = filter(items) { candidate: SeasonalFreightInvoiceProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFreightInvoiceProposals(store: SeasonalFreightInvoiceProposalStore, query: SeasonalFreightInvoiceProposalQuery): MutableList<SeasonalFreightInvoiceProposal> {
    return query.apply(store.list())
}
