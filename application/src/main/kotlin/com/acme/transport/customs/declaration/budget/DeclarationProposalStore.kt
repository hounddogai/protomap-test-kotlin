package com.acme.transport.customs.declaration.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration proposals by code. */
interface DeclarationProposalStore {
    fun find(code: String): DeclarationProposal?
    fun save(item: DeclarationProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationProposalStore(
    var items: MutableMap<String, DeclarationProposal>,
    var order: MutableList<String>,
) : DeclarationProposalStore {
    override fun find(code: String): DeclarationProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationProposal) {
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

    override fun list(): MutableList<DeclarationProposal> {
        val out = mutableListOf<DeclarationProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationProposalStore(): DeclarationProposalStore {
    return MemoryDeclarationProposalStore(mutableMapOf<String, DeclarationProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationProposalStore(
    var inner: DeclarationProposalStore,
    var cache: TtlCache<DeclarationProposal>,
    var hits: Int,
    var misses: Int,
) : DeclarationProposalStore {
    override fun find(code: String): DeclarationProposal? {
        val hit = (cache.get(code) as DeclarationProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationProposal> = inner.list()
}

fun newCachedDeclarationProposalStore(inner: DeclarationProposalStore, ttl: Int, clock: Clock): DeclarationProposalStore {
    return CachedDeclarationProposalStore(
        inner = inner,
        cache = newTtlCache<DeclarationProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration proposals. Empty fields match all. */
data class DeclarationProposalQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationProposal>): MutableList<DeclarationProposal> {
        val matched = filter(items) { candidate: DeclarationProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationProposals(store: DeclarationProposalStore, query: DeclarationProposalQuery): MutableList<DeclarationProposal> {
    return query.apply(store.list())
}
