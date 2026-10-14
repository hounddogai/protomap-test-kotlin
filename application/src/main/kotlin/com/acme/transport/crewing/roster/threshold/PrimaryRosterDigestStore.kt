package com.acme.transport.crewing.roster.threshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary roster digests by code. */
interface PrimaryRosterDigestStore {
    fun find(code: String): PrimaryRosterDigest?
    fun save(item: PrimaryRosterDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRosterDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRosterDigestStore(
    var items: MutableMap<String, PrimaryRosterDigest>,
    var order: MutableList<String>,
) : PrimaryRosterDigestStore {
    override fun find(code: String): PrimaryRosterDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRosterDigest) {
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

    override fun list(): MutableList<PrimaryRosterDigest> {
        val out = mutableListOf<PrimaryRosterDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRosterDigestStore(): PrimaryRosterDigestStore {
    return MemoryPrimaryRosterDigestStore(mutableMapOf<String, PrimaryRosterDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRosterDigestStore(
    var inner: PrimaryRosterDigestStore,
    var cache: TtlCache<PrimaryRosterDigest>,
    var hits: Int,
    var misses: Int,
) : PrimaryRosterDigestStore {
    override fun find(code: String): PrimaryRosterDigest? {
        val hit = (cache.get(code) as PrimaryRosterDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRosterDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRosterDigest> = inner.list()
}

fun newCachedPrimaryRosterDigestStore(inner: PrimaryRosterDigestStore, ttl: Int, clock: Clock): PrimaryRosterDigestStore {
    return CachedPrimaryRosterDigestStore(
        inner = inner,
        cache = newTtlCache<PrimaryRosterDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary roster digests. Empty fields match all. */
data class PrimaryRosterDigestQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRosterDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRosterDigest>): MutableList<PrimaryRosterDigest> {
        val matched = filter(items) { candidate: PrimaryRosterDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRosterDigests(store: PrimaryRosterDigestStore, query: PrimaryRosterDigestQuery): MutableList<PrimaryRosterDigest> {
    return query.apply(store.list())
}
