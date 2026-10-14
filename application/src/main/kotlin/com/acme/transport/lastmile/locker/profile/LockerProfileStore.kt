package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker profiles by code. */
interface LockerProfileStore {
    fun find(code: String): LockerProfile?
    fun save(item: LockerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerProfileStore(
    var items: MutableMap<String, LockerProfile>,
    var order: MutableList<String>,
) : LockerProfileStore {
    override fun find(code: String): LockerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerProfile) {
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

    override fun list(): MutableList<LockerProfile> {
        val out = mutableListOf<LockerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerProfileStore(): LockerProfileStore {
    return MemoryLockerProfileStore(mutableMapOf<String, LockerProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerProfileStore(
    var inner: LockerProfileStore,
    var cache: TtlCache<LockerProfile>,
    var hits: Int,
    var misses: Int,
) : LockerProfileStore {
    override fun find(code: String): LockerProfile? {
        val hit = (cache.get(code) as LockerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerProfile> = inner.list()
}

fun newCachedLockerProfileStore(inner: LockerProfileStore, ttl: Int, clock: Clock): LockerProfileStore {
    return CachedLockerProfileStore(
        inner = inner,
        cache = newTtlCache<LockerProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker profiles. Empty fields match all. */
data class LockerProfileQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerProfile): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerProfile>): MutableList<LockerProfile> {
        val matched = filter(items) { candidate: LockerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerProfiles(store: LockerProfileStore, query: LockerProfileQuery): MutableList<LockerProfile> {
    return query.apply(store.list())
}
