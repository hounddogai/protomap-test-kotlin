package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial proposals by code. */
interface AccessorialProposalStore {
    fun find(code: String): AccessorialProposal?
    fun save(item: AccessorialProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialProposalStore(
    var items: MutableMap<String, AccessorialProposal>,
    var order: MutableList<String>,
) : AccessorialProposalStore {
    override fun find(code: String): AccessorialProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialProposal) {
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

    override fun list(): MutableList<AccessorialProposal> {
        val out = mutableListOf<AccessorialProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialProposalStore(): AccessorialProposalStore {
    return MemoryAccessorialProposalStore(mutableMapOf<String, AccessorialProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialProposalStore(
    var inner: AccessorialProposalStore,
    var cache: TtlCache<AccessorialProposal>,
    var hits: Int,
    var misses: Int,
) : AccessorialProposalStore {
    override fun find(code: String): AccessorialProposal? {
        val hit = (cache.get(code) as AccessorialProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialProposal> = inner.list()
}

fun newCachedAccessorialProposalStore(inner: AccessorialProposalStore, ttl: Int, clock: Clock): AccessorialProposalStore {
    return CachedAccessorialProposalStore(
        inner = inner,
        cache = newTtlCache<AccessorialProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial proposals. Empty fields match all. */
data class AccessorialProposalQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialProposal): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialProposal>): MutableList<AccessorialProposal> {
        val matched = filter(items) { candidate: AccessorialProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialProposals(store: AccessorialProposalStore, query: AccessorialProposalQuery): MutableList<AccessorialProposal> {
    return query.apply(store.list())
}
