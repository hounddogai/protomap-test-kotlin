package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster groups by code. */
interface RosterGroupStore {
    fun find(code: String): RosterGroup?
    fun save(item: RosterGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterGroupStore(
    var items: MutableMap<String, RosterGroup>,
    var order: MutableList<String>,
) : RosterGroupStore {
    override fun find(code: String): RosterGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterGroup) {
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

    override fun list(): MutableList<RosterGroup> {
        val out = mutableListOf<RosterGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterGroupStore(): RosterGroupStore {
    return MemoryRosterGroupStore(mutableMapOf<String, RosterGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterGroupStore(
    var inner: RosterGroupStore,
    var cache: TtlCache<RosterGroup>,
    var hits: Int,
    var misses: Int,
) : RosterGroupStore {
    override fun find(code: String): RosterGroup? {
        val hit = (cache.get(code) as RosterGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterGroup> = inner.list()
}

fun newCachedRosterGroupStore(inner: RosterGroupStore, ttl: Int, clock: Clock): RosterGroupStore {
    return CachedRosterGroupStore(
        inner = inner,
        cache = newTtlCache<RosterGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster groups. Empty fields match all. */
data class RosterGroupQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterGroup>): MutableList<RosterGroup> {
        val matched = filter(items) { candidate: RosterGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterGroups(store: RosterGroupStore, query: RosterGroupQuery): MutableList<RosterGroup> {
    return query.apply(store.list())
}
