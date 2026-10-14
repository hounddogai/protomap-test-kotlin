package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional camera event profiles by code. */
interface RegionalCameraEventProfileStore {
    fun find(code: String): RegionalCameraEventProfile?
    fun save(item: RegionalCameraEventProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCameraEventProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCameraEventProfileStore(
    var items: MutableMap<String, RegionalCameraEventProfile>,
    var order: MutableList<String>,
) : RegionalCameraEventProfileStore {
    override fun find(code: String): RegionalCameraEventProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCameraEventProfile) {
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

    override fun list(): MutableList<RegionalCameraEventProfile> {
        val out = mutableListOf<RegionalCameraEventProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCameraEventProfileStore(): RegionalCameraEventProfileStore {
    return MemoryRegionalCameraEventProfileStore(
        mutableMapOf<String, RegionalCameraEventProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCameraEventProfileStore(
    var inner: RegionalCameraEventProfileStore,
    var cache: TtlCache<RegionalCameraEventProfile>,
    var hits: Int,
    var misses: Int,
) : RegionalCameraEventProfileStore {
    override fun find(code: String): RegionalCameraEventProfile? {
        val hit = (cache.get(code) as RegionalCameraEventProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCameraEventProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCameraEventProfile> = inner.list()
}

fun newCachedRegionalCameraEventProfileStore(inner: RegionalCameraEventProfileStore, ttl: Int, clock: Clock): RegionalCameraEventProfileStore {
    return CachedRegionalCameraEventProfileStore(
        inner = inner,
        cache = newTtlCache<RegionalCameraEventProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional camera event profiles. Empty fields match all. */
data class RegionalCameraEventProfileQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCameraEventProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCameraEventProfile>): MutableList<RegionalCameraEventProfile> {
        val matched = filter(items) { candidate: RegionalCameraEventProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCameraEventProfiles(store: RegionalCameraEventProfileStore, query: RegionalCameraEventProfileQuery): MutableList<RegionalCameraEventProfile> {
    return query.apply(store.list())
}
