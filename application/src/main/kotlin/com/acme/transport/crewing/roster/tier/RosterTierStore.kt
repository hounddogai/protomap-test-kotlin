package com.acme.transport.crewing.roster.tier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster tiers by code. */
interface RosterTierStore {
    fun find(code: String): RosterTier?
    fun save(item: RosterTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterTierStore(
    var items: MutableMap<String, RosterTier>,
    var order: MutableList<String>,
) : RosterTierStore {
    override fun find(code: String): RosterTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterTier) {
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

    override fun list(): MutableList<RosterTier> {
        val out = mutableListOf<RosterTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterTierStore(): RosterTierStore {
    return MemoryRosterTierStore(mutableMapOf<String, RosterTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterTierStore(
    var inner: RosterTierStore,
    var cache: TtlCache<RosterTier>,
    var hits: Int,
    var misses: Int,
) : RosterTierStore {
    override fun find(code: String): RosterTier? {
        val hit = (cache.get(code) as RosterTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterTier> = inner.list()
}

fun newCachedRosterTierStore(inner: RosterTierStore, ttl: Int, clock: Clock): RosterTierStore {
    return CachedRosterTierStore(
        inner = inner,
        cache = newTtlCache<RosterTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster tiers. Empty fields match all. */
data class RosterTierQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterTier>): MutableList<RosterTier> {
        val matched = filter(items) { candidate: RosterTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterTiers(store: RosterTierStore, query: RosterTierQuery): MutableList<RosterTier> {
    return query.apply(store.list())
}
