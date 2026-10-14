package com.acme.transport.crewing.qualification.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification proposals by code. */
interface QualificationProposalStore {
    fun find(code: String): QualificationProposal?
    fun save(item: QualificationProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationProposalStore(
    var items: MutableMap<String, QualificationProposal>,
    var order: MutableList<String>,
) : QualificationProposalStore {
    override fun find(code: String): QualificationProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationProposal) {
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

    override fun list(): MutableList<QualificationProposal> {
        val out = mutableListOf<QualificationProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationProposalStore(): QualificationProposalStore {
    return MemoryQualificationProposalStore(
        mutableMapOf<String, QualificationProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationProposalStore(
    var inner: QualificationProposalStore,
    var cache: TtlCache<QualificationProposal>,
    var hits: Int,
    var misses: Int,
) : QualificationProposalStore {
    override fun find(code: String): QualificationProposal? {
        val hit = (cache.get(code) as QualificationProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationProposal> = inner.list()
}

fun newCachedQualificationProposalStore(inner: QualificationProposalStore, ttl: Int, clock: Clock): QualificationProposalStore {
    return CachedQualificationProposalStore(
        inner = inner,
        cache = newTtlCache<QualificationProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification proposals. Empty fields match all. */
data class QualificationProposalQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationProposal): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<QualificationProposal>): MutableList<QualificationProposal> {
        val matched = filter(items) { candidate: QualificationProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationProposals(store: QualificationProposalStore, query: QualificationProposalQuery): MutableList<QualificationProposal> {
    return query.apply(store.list())
}
