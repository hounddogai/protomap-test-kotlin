package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive qualification proposals by code. */
interface AdaptiveQualificationProposalStore {
    fun find(code: String): AdaptiveQualificationProposal?
    fun save(item: AdaptiveQualificationProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveQualificationProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveQualificationProposalStore(
    var items: MutableMap<String, AdaptiveQualificationProposal>,
    var order: MutableList<String>,
) : AdaptiveQualificationProposalStore {
    override fun find(code: String): AdaptiveQualificationProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveQualificationProposal) {
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

    override fun list(): MutableList<AdaptiveQualificationProposal> {
        val out = mutableListOf<AdaptiveQualificationProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveQualificationProposalStore(): AdaptiveQualificationProposalStore {
    return MemoryAdaptiveQualificationProposalStore(
        mutableMapOf<String, AdaptiveQualificationProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveQualificationProposalStore(
    var inner: AdaptiveQualificationProposalStore,
    var cache: TtlCache<AdaptiveQualificationProposal>,
    var hits: Int,
    var misses: Int,
) : AdaptiveQualificationProposalStore {
    override fun find(code: String): AdaptiveQualificationProposal? {
        val hit = (cache.get(code) as AdaptiveQualificationProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveQualificationProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveQualificationProposal> = inner.list()
}

fun newCachedAdaptiveQualificationProposalStore(inner: AdaptiveQualificationProposalStore, ttl: Int, clock: Clock): AdaptiveQualificationProposalStore {
    return CachedAdaptiveQualificationProposalStore(
        inner = inner,
        cache = newTtlCache<AdaptiveQualificationProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive qualification proposals. Empty fields match all. */
data class AdaptiveQualificationProposalQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveQualificationProposal): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveQualificationProposal>): MutableList<AdaptiveQualificationProposal> {
        val matched = filter(items) { candidate: AdaptiveQualificationProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveQualificationProposals(store: AdaptiveQualificationProposalStore, query: AdaptiveQualificationProposalQuery): MutableList<AdaptiveQualificationProposal> {
    return query.apply(store.list())
}
