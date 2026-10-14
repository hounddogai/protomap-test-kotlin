package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance profiles by code. */
interface ClearanceProfileStore {
    fun find(code: String): ClearanceProfile?
    fun save(item: ClearanceProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceProfileStore(
    var items: MutableMap<String, ClearanceProfile>,
    var order: MutableList<String>,
) : ClearanceProfileStore {
    override fun find(code: String): ClearanceProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceProfile) {
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

    override fun list(): MutableList<ClearanceProfile> {
        val out = mutableListOf<ClearanceProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceProfileStore(): ClearanceProfileStore {
    return MemoryClearanceProfileStore(mutableMapOf<String, ClearanceProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceProfileStore(
    var inner: ClearanceProfileStore,
    var cache: TtlCache<ClearanceProfile>,
    var hits: Int,
    var misses: Int,
) : ClearanceProfileStore {
    override fun find(code: String): ClearanceProfile? {
        val hit = (cache.get(code) as ClearanceProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceProfile> = inner.list()
}

fun newCachedClearanceProfileStore(inner: ClearanceProfileStore, ttl: Int, clock: Clock): ClearanceProfileStore {
    return CachedClearanceProfileStore(
        inner = inner,
        cache = newTtlCache<ClearanceProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance profiles. Empty fields match all. */
data class ClearanceProfileQuery(
    var channel: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceProfile): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ClearanceProfile>): MutableList<ClearanceProfile> {
        val matched = filter(items) { candidate: ClearanceProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceProfiles(store: ClearanceProfileStore, query: ClearanceProfileQuery): MutableList<ClearanceProfile> {
    return query.apply(store.list())
}
