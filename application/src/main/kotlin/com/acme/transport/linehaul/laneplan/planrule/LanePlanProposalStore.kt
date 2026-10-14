package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan proposals by code. */
interface LanePlanProposalStore {
    fun find(code: String): LanePlanProposal?
    fun save(item: LanePlanProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanProposalStore(
    var items: MutableMap<String, LanePlanProposal>,
    var order: MutableList<String>,
) : LanePlanProposalStore {
    override fun find(code: String): LanePlanProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanProposal) {
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

    override fun list(): MutableList<LanePlanProposal> {
        val out = mutableListOf<LanePlanProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanProposalStore(): LanePlanProposalStore {
    return MemoryLanePlanProposalStore(mutableMapOf<String, LanePlanProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanProposalStore(
    var inner: LanePlanProposalStore,
    var cache: TtlCache<LanePlanProposal>,
    var hits: Int,
    var misses: Int,
) : LanePlanProposalStore {
    override fun find(code: String): LanePlanProposal? {
        val hit = (cache.get(code) as LanePlanProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanProposal> = inner.list()
}

fun newCachedLanePlanProposalStore(inner: LanePlanProposalStore, ttl: Int, clock: Clock): LanePlanProposalStore {
    return CachedLanePlanProposalStore(
        inner = inner,
        cache = newTtlCache<LanePlanProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan proposals. Empty fields match all. */
data class LanePlanProposalQuery(
    var locale: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanProposal): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanProposal>): MutableList<LanePlanProposal> {
        val matched = filter(items) { candidate: LanePlanProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanProposals(store: LanePlanProposalStore, query: LanePlanProposalQuery): MutableList<LanePlanProposal> {
    return query.apply(store.list())
}
