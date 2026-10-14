package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending idling profiles by code. */
interface PendingIdlingProfileStore {
    fun find(code: String): PendingIdlingProfile?
    fun save(item: PendingIdlingProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingIdlingProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingIdlingProfileStore(
    var items: MutableMap<String, PendingIdlingProfile>,
    var order: MutableList<String>,
) : PendingIdlingProfileStore {
    override fun find(code: String): PendingIdlingProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingIdlingProfile) {
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

    override fun list(): MutableList<PendingIdlingProfile> {
        val out = mutableListOf<PendingIdlingProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingIdlingProfileStore(): PendingIdlingProfileStore {
    return MemoryPendingIdlingProfileStore(
        mutableMapOf<String, PendingIdlingProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingIdlingProfileStore(
    var inner: PendingIdlingProfileStore,
    var cache: TtlCache<PendingIdlingProfile>,
    var hits: Int,
    var misses: Int,
) : PendingIdlingProfileStore {
    override fun find(code: String): PendingIdlingProfile? {
        val hit = (cache.get(code) as PendingIdlingProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingIdlingProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingIdlingProfile> = inner.list()
}

fun newCachedPendingIdlingProfileStore(inner: PendingIdlingProfileStore, ttl: Int, clock: Clock): PendingIdlingProfileStore {
    return CachedPendingIdlingProfileStore(
        inner = inner,
        cache = newTtlCache<PendingIdlingProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending idling profiles. Empty fields match all. */
data class PendingIdlingProfileQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingIdlingProfile): Boolean {
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

    fun apply(items: MutableList<PendingIdlingProfile>): MutableList<PendingIdlingProfile> {
        val matched = filter(items) { candidate: PendingIdlingProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingIdlingProfiles(store: PendingIdlingProfileStore, query: PendingIdlingProfileQuery): MutableList<PendingIdlingProfile> {
    return query.apply(store.list())
}
