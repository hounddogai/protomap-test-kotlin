package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy locker profiles by code. */
interface LegacyLockerProfileStore {
    fun find(code: String): LegacyLockerProfile?
    fun save(item: LegacyLockerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLockerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLockerProfileStore(
    var items: MutableMap<String, LegacyLockerProfile>,
    var order: MutableList<String>,
) : LegacyLockerProfileStore {
    override fun find(code: String): LegacyLockerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLockerProfile) {
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

    override fun list(): MutableList<LegacyLockerProfile> {
        val out = mutableListOf<LegacyLockerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLockerProfileStore(): LegacyLockerProfileStore {
    return MemoryLegacyLockerProfileStore(mutableMapOf<String, LegacyLockerProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLockerProfileStore(
    var inner: LegacyLockerProfileStore,
    var cache: TtlCache<LegacyLockerProfile>,
    var hits: Int,
    var misses: Int,
) : LegacyLockerProfileStore {
    override fun find(code: String): LegacyLockerProfile? {
        val hit = (cache.get(code) as LegacyLockerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLockerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLockerProfile> = inner.list()
}

fun newCachedLegacyLockerProfileStore(inner: LegacyLockerProfileStore, ttl: Int, clock: Clock): LegacyLockerProfileStore {
    return CachedLegacyLockerProfileStore(
        inner = inner,
        cache = newTtlCache<LegacyLockerProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy locker profiles. Empty fields match all. */
data class LegacyLockerProfileQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLockerProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLockerProfile>): MutableList<LegacyLockerProfile> {
        val matched = filter(items) { candidate: LegacyLockerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLockerProfiles(store: LegacyLockerProfileStore, query: LegacyLockerProfileQuery): MutableList<LegacyLockerProfile> {
    return query.apply(store.list())
}
