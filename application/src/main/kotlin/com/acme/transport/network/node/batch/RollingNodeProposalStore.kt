package com.acme.transport.network.node.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling node proposals by code. */
interface RollingNodeProposalStore {
    fun find(code: String): RollingNodeProposal?
    fun save(item: RollingNodeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNodeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNodeProposalStore(
    var items: MutableMap<String, RollingNodeProposal>,
    var order: MutableList<String>,
) : RollingNodeProposalStore {
    override fun find(code: String): RollingNodeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNodeProposal) {
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

    override fun list(): MutableList<RollingNodeProposal> {
        val out = mutableListOf<RollingNodeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNodeProposalStore(): RollingNodeProposalStore {
    return MemoryRollingNodeProposalStore(mutableMapOf<String, RollingNodeProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNodeProposalStore(
    var inner: RollingNodeProposalStore,
    var cache: TtlCache<RollingNodeProposal>,
    var hits: Int,
    var misses: Int,
) : RollingNodeProposalStore {
    override fun find(code: String): RollingNodeProposal? {
        val hit = (cache.get(code) as RollingNodeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNodeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNodeProposal> = inner.list()
}

fun newCachedRollingNodeProposalStore(inner: RollingNodeProposalStore, ttl: Int, clock: Clock): RollingNodeProposalStore {
    return CachedRollingNodeProposalStore(
        inner = inner,
        cache = newTtlCache<RollingNodeProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling node proposals. Empty fields match all. */
data class RollingNodeProposalQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNodeProposal): Boolean {
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

    fun apply(items: MutableList<RollingNodeProposal>): MutableList<RollingNodeProposal> {
        val matched = filter(items) { candidate: RollingNodeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNodeProposals(store: RollingNodeProposalStore, query: RollingNodeProposalQuery): MutableList<RollingNodeProposal> {
    return query.apply(store.list())
}
