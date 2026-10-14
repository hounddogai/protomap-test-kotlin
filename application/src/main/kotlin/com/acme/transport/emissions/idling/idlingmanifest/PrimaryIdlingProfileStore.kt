package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary idling profiles by code. */
interface PrimaryIdlingProfileStore {
    fun find(code: String): PrimaryIdlingProfile?
    fun save(item: PrimaryIdlingProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryIdlingProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryIdlingProfileStore(
    var items: MutableMap<String, PrimaryIdlingProfile>,
    var order: MutableList<String>,
) : PrimaryIdlingProfileStore {
    override fun find(code: String): PrimaryIdlingProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryIdlingProfile) {
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

    override fun list(): MutableList<PrimaryIdlingProfile> {
        val out = mutableListOf<PrimaryIdlingProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryIdlingProfileStore(): PrimaryIdlingProfileStore {
    return MemoryPrimaryIdlingProfileStore(
        mutableMapOf<String, PrimaryIdlingProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryIdlingProfileStore(
    var inner: PrimaryIdlingProfileStore,
    var cache: TtlCache<PrimaryIdlingProfile>,
    var hits: Int,
    var misses: Int,
) : PrimaryIdlingProfileStore {
    override fun find(code: String): PrimaryIdlingProfile? {
        val hit = (cache.get(code) as PrimaryIdlingProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryIdlingProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryIdlingProfile> = inner.list()
}

fun newCachedPrimaryIdlingProfileStore(inner: PrimaryIdlingProfileStore, ttl: Int, clock: Clock): PrimaryIdlingProfileStore {
    return CachedPrimaryIdlingProfileStore(
        inner = inner,
        cache = newTtlCache<PrimaryIdlingProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary idling profiles. Empty fields match all. */
data class PrimaryIdlingProfileQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryIdlingProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PrimaryIdlingProfile>): MutableList<PrimaryIdlingProfile> {
        val matched = filter(items) { candidate: PrimaryIdlingProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryIdlingProfiles(store: PrimaryIdlingProfileStore, query: PrimaryIdlingProfileQuery): MutableList<PrimaryIdlingProfile> {
    return query.apply(store.list())
}
