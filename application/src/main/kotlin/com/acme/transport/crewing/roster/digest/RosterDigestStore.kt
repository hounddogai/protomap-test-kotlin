package com.acme.transport.crewing.roster.digest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster digests by code. */
interface RosterDigestStore {
    fun find(code: String): RosterDigest?
    fun save(item: RosterDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterDigestStore(
    var items: MutableMap<String, RosterDigest>,
    var order: MutableList<String>,
) : RosterDigestStore {
    override fun find(code: String): RosterDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterDigest) {
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

    override fun list(): MutableList<RosterDigest> {
        val out = mutableListOf<RosterDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterDigestStore(): RosterDigestStore {
    return MemoryRosterDigestStore(mutableMapOf<String, RosterDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterDigestStore(
    var inner: RosterDigestStore,
    var cache: TtlCache<RosterDigest>,
    var hits: Int,
    var misses: Int,
) : RosterDigestStore {
    override fun find(code: String): RosterDigest? {
        val hit = (cache.get(code) as RosterDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterDigest> = inner.list()
}

fun newCachedRosterDigestStore(inner: RosterDigestStore, ttl: Int, clock: Clock): RosterDigestStore {
    return CachedRosterDigestStore(
        inner = inner,
        cache = newTtlCache<RosterDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster digests. Empty fields match all. */
data class RosterDigestQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterDigest>): MutableList<RosterDigest> {
        val matched = filter(items) { candidate: RosterDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterDigests(store: RosterDigestStore, query: RosterDigestQuery): MutableList<RosterDigest> {
    return query.apply(store.list())
}
