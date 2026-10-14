package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster profiles by code. */
interface RosterProfileStore {
    fun find(code: String): RosterProfile?
    fun save(item: RosterProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterProfileStore(
    var items: MutableMap<String, RosterProfile>,
    var order: MutableList<String>,
) : RosterProfileStore {
    override fun find(code: String): RosterProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterProfile) {
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

    override fun list(): MutableList<RosterProfile> {
        val out = mutableListOf<RosterProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterProfileStore(): RosterProfileStore {
    return MemoryRosterProfileStore(mutableMapOf<String, RosterProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterProfileStore(
    var inner: RosterProfileStore,
    var cache: TtlCache<RosterProfile>,
    var hits: Int,
    var misses: Int,
) : RosterProfileStore {
    override fun find(code: String): RosterProfile? {
        val hit = (cache.get(code) as RosterProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterProfile> = inner.list()
}

fun newCachedRosterProfileStore(inner: RosterProfileStore, ttl: Int, clock: Clock): RosterProfileStore {
    return CachedRosterProfileStore(
        inner = inner,
        cache = newTtlCache<RosterProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster profiles. Empty fields match all. */
data class RosterProfileQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterProfile): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<RosterProfile>): MutableList<RosterProfile> {
        val matched = filter(items) { candidate: RosterProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterProfiles(store: RosterProfileStore, query: RosterProfileQuery): MutableList<RosterProfile> {
    return query.apply(store.list())
}
