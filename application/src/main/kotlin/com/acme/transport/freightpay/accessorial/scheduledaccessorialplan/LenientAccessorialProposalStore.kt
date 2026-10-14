package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient accessorial proposals by code. */
interface LenientAccessorialProposalStore {
    fun find(code: String): LenientAccessorialProposal?
    fun save(item: LenientAccessorialProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAccessorialProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAccessorialProposalStore(
    var items: MutableMap<String, LenientAccessorialProposal>,
    var order: MutableList<String>,
) : LenientAccessorialProposalStore {
    override fun find(code: String): LenientAccessorialProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAccessorialProposal) {
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

    override fun list(): MutableList<LenientAccessorialProposal> {
        val out = mutableListOf<LenientAccessorialProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAccessorialProposalStore(): LenientAccessorialProposalStore {
    return MemoryLenientAccessorialProposalStore(
        mutableMapOf<String, LenientAccessorialProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAccessorialProposalStore(
    var inner: LenientAccessorialProposalStore,
    var cache: TtlCache<LenientAccessorialProposal>,
    var hits: Int,
    var misses: Int,
) : LenientAccessorialProposalStore {
    override fun find(code: String): LenientAccessorialProposal? {
        val hit = (cache.get(code) as LenientAccessorialProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAccessorialProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAccessorialProposal> = inner.list()
}

fun newCachedLenientAccessorialProposalStore(inner: LenientAccessorialProposalStore, ttl: Int, clock: Clock): LenientAccessorialProposalStore {
    return CachedLenientAccessorialProposalStore(
        inner = inner,
        cache = newTtlCache<LenientAccessorialProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient accessorial proposals. Empty fields match all. */
data class LenientAccessorialProposalQuery(
    var locale: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAccessorialProposal): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<LenientAccessorialProposal>): MutableList<LenientAccessorialProposal> {
        val matched = filter(items) { candidate: LenientAccessorialProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAccessorialProposals(store: LenientAccessorialProposalStore, query: LenientAccessorialProposalQuery): MutableList<LenientAccessorialProposal> {
    return query.apply(store.list())
}
