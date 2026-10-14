package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary drop profiles by code. */
interface PrimaryDropProfileStore {
    fun find(code: String): PrimaryDropProfile?
    fun save(item: PrimaryDropProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDropProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDropProfileStore(
    var items: MutableMap<String, PrimaryDropProfile>,
    var order: MutableList<String>,
) : PrimaryDropProfileStore {
    override fun find(code: String): PrimaryDropProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDropProfile) {
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

    override fun list(): MutableList<PrimaryDropProfile> {
        val out = mutableListOf<PrimaryDropProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDropProfileStore(): PrimaryDropProfileStore {
    return MemoryPrimaryDropProfileStore(mutableMapOf<String, PrimaryDropProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDropProfileStore(
    var inner: PrimaryDropProfileStore,
    var cache: TtlCache<PrimaryDropProfile>,
    var hits: Int,
    var misses: Int,
) : PrimaryDropProfileStore {
    override fun find(code: String): PrimaryDropProfile? {
        val hit = (cache.get(code) as PrimaryDropProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDropProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDropProfile> = inner.list()
}

fun newCachedPrimaryDropProfileStore(inner: PrimaryDropProfileStore, ttl: Int, clock: Clock): PrimaryDropProfileStore {
    return CachedPrimaryDropProfileStore(
        inner = inner,
        cache = newTtlCache<PrimaryDropProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary drop profiles. Empty fields match all. */
data class PrimaryDropProfileQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDropProfile): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDropProfile>): MutableList<PrimaryDropProfile> {
        val matched = filter(items) { candidate: PrimaryDropProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDropProfiles(store: PrimaryDropProfileStore, query: PrimaryDropProfileQuery): MutableList<PrimaryDropProfile> {
    return query.apply(store.list())
}
