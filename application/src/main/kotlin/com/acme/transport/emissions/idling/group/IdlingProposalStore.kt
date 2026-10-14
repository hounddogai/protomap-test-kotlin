package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling proposals by code. */
interface IdlingProposalStore {
    fun find(code: String): IdlingProposal?
    fun save(item: IdlingProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingProposalStore(
    var items: MutableMap<String, IdlingProposal>,
    var order: MutableList<String>,
) : IdlingProposalStore {
    override fun find(code: String): IdlingProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingProposal) {
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

    override fun list(): MutableList<IdlingProposal> {
        val out = mutableListOf<IdlingProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingProposalStore(): IdlingProposalStore {
    return MemoryIdlingProposalStore(mutableMapOf<String, IdlingProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingProposalStore(
    var inner: IdlingProposalStore,
    var cache: TtlCache<IdlingProposal>,
    var hits: Int,
    var misses: Int,
) : IdlingProposalStore {
    override fun find(code: String): IdlingProposal? {
        val hit = (cache.get(code) as IdlingProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingProposal> = inner.list()
}

fun newCachedIdlingProposalStore(inner: IdlingProposalStore, ttl: Int, clock: Clock): IdlingProposalStore {
    return CachedIdlingProposalStore(
        inner = inner,
        cache = newTtlCache<IdlingProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling proposals. Empty fields match all. */
data class IdlingProposalQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingProposal>): MutableList<IdlingProposal> {
        val matched = filter(items) { candidate: IdlingProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingProposals(store: IdlingProposalStore, query: IdlingProposalQuery): MutableList<IdlingProposal> {
    return query.apply(store.list())
}
