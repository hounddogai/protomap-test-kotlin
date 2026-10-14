package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone proposals by code. */
interface ZoneProposalStore {
    fun find(code: String): ZoneProposal?
    fun save(item: ZoneProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneProposalStore(
    var items: MutableMap<String, ZoneProposal>,
    var order: MutableList<String>,
) : ZoneProposalStore {
    override fun find(code: String): ZoneProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneProposal) {
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

    override fun list(): MutableList<ZoneProposal> {
        val out = mutableListOf<ZoneProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneProposalStore(): ZoneProposalStore {
    return MemoryZoneProposalStore(mutableMapOf<String, ZoneProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneProposalStore(
    var inner: ZoneProposalStore,
    var cache: TtlCache<ZoneProposal>,
    var hits: Int,
    var misses: Int,
) : ZoneProposalStore {
    override fun find(code: String): ZoneProposal? {
        val hit = (cache.get(code) as ZoneProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneProposal> = inner.list()
}

fun newCachedZoneProposalStore(inner: ZoneProposalStore, ttl: Int, clock: Clock): ZoneProposalStore {
    return CachedZoneProposalStore(
        inner = inner,
        cache = newTtlCache<ZoneProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone proposals. Empty fields match all. */
data class ZoneProposalQuery(
    var locale: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneProposal): Boolean {
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

    fun apply(items: MutableList<ZoneProposal>): MutableList<ZoneProposal> {
        val matched = filter(items) { candidate: ZoneProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneProposals(store: ZoneProposalStore, query: ZoneProposalQuery): MutableList<ZoneProposal> {
    return query.apply(store.list())
}
