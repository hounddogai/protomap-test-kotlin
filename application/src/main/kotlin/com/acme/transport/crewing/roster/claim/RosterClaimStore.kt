package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster claims by code. */
interface RosterClaimStore {
    fun find(code: String): RosterClaim?
    fun save(item: RosterClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterClaimStore(
    var items: MutableMap<String, RosterClaim>,
    var order: MutableList<String>,
) : RosterClaimStore {
    override fun find(code: String): RosterClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterClaim) {
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

    override fun list(): MutableList<RosterClaim> {
        val out = mutableListOf<RosterClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterClaimStore(): RosterClaimStore {
    return MemoryRosterClaimStore(mutableMapOf<String, RosterClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterClaimStore(
    var inner: RosterClaimStore,
    var cache: TtlCache<RosterClaim>,
    var hits: Int,
    var misses: Int,
) : RosterClaimStore {
    override fun find(code: String): RosterClaim? {
        val hit = (cache.get(code) as RosterClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterClaim> = inner.list()
}

fun newCachedRosterClaimStore(inner: RosterClaimStore, ttl: Int, clock: Clock): RosterClaimStore {
    return CachedRosterClaimStore(
        inner = inner,
        cache = newTtlCache<RosterClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster claims. Empty fields match all. */
data class RosterClaimQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterClaim>): MutableList<RosterClaim> {
        val matched = filter(items) { candidate: RosterClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterClaims(store: RosterClaimStore, query: RosterClaimQuery): MutableList<RosterClaim> {
    return query.apply(store.list())
}
