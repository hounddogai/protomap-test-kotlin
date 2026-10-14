package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default scorecard proposals by code. */
interface DefaultScorecardProposalStore {
    fun find(code: String): DefaultScorecardProposal?
    fun save(item: DefaultScorecardProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultScorecardProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultScorecardProposalStore(
    var items: MutableMap<String, DefaultScorecardProposal>,
    var order: MutableList<String>,
) : DefaultScorecardProposalStore {
    override fun find(code: String): DefaultScorecardProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultScorecardProposal) {
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

    override fun list(): MutableList<DefaultScorecardProposal> {
        val out = mutableListOf<DefaultScorecardProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultScorecardProposalStore(): DefaultScorecardProposalStore {
    return MemoryDefaultScorecardProposalStore(
        mutableMapOf<String, DefaultScorecardProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultScorecardProposalStore(
    var inner: DefaultScorecardProposalStore,
    var cache: TtlCache<DefaultScorecardProposal>,
    var hits: Int,
    var misses: Int,
) : DefaultScorecardProposalStore {
    override fun find(code: String): DefaultScorecardProposal? {
        val hit = (cache.get(code) as DefaultScorecardProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultScorecardProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultScorecardProposal> = inner.list()
}

fun newCachedDefaultScorecardProposalStore(inner: DefaultScorecardProposalStore, ttl: Int, clock: Clock): DefaultScorecardProposalStore {
    return CachedDefaultScorecardProposalStore(
        inner = inner,
        cache = newTtlCache<DefaultScorecardProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default scorecard proposals. Empty fields match all. */
data class DefaultScorecardProposalQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultScorecardProposal): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<DefaultScorecardProposal>): MutableList<DefaultScorecardProposal> {
        val matched = filter(items) { candidate: DefaultScorecardProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultScorecardProposals(store: DefaultScorecardProposalStore, query: DefaultScorecardProposalQuery): MutableList<DefaultScorecardProposal> {
    return query.apply(store.list())
}
