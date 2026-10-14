package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional locker profiles by code. */
interface RegionalLockerProfileStore {
    fun find(code: String): RegionalLockerProfile?
    fun save(item: RegionalLockerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLockerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLockerProfileStore(
    var items: MutableMap<String, RegionalLockerProfile>,
    var order: MutableList<String>,
) : RegionalLockerProfileStore {
    override fun find(code: String): RegionalLockerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLockerProfile) {
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

    override fun list(): MutableList<RegionalLockerProfile> {
        val out = mutableListOf<RegionalLockerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLockerProfileStore(): RegionalLockerProfileStore {
    return MemoryRegionalLockerProfileStore(
        mutableMapOf<String, RegionalLockerProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLockerProfileStore(
    var inner: RegionalLockerProfileStore,
    var cache: TtlCache<RegionalLockerProfile>,
    var hits: Int,
    var misses: Int,
) : RegionalLockerProfileStore {
    override fun find(code: String): RegionalLockerProfile? {
        val hit = (cache.get(code) as RegionalLockerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLockerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLockerProfile> = inner.list()
}

fun newCachedRegionalLockerProfileStore(inner: RegionalLockerProfileStore, ttl: Int, clock: Clock): RegionalLockerProfileStore {
    return CachedRegionalLockerProfileStore(
        inner = inner,
        cache = newTtlCache<RegionalLockerProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional locker profiles. Empty fields match all. */
data class RegionalLockerProfileQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLockerProfile): Boolean {
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

    fun apply(items: MutableList<RegionalLockerProfile>): MutableList<RegionalLockerProfile> {
        val matched = filter(items) { candidate: RegionalLockerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLockerProfiles(store: RegionalLockerProfileStore, query: RegionalLockerProfileQuery): MutableList<RegionalLockerProfile> {
    return query.apply(store.list())
}
