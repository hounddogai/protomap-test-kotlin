package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity profiles by code. */
interface CapacityProfileStore {
    fun find(code: String): CapacityProfile?
    fun save(item: CapacityProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityProfileStore(
    var items: MutableMap<String, CapacityProfile>,
    var order: MutableList<String>,
) : CapacityProfileStore {
    override fun find(code: String): CapacityProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityProfile) {
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

    override fun list(): MutableList<CapacityProfile> {
        val out = mutableListOf<CapacityProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityProfileStore(): CapacityProfileStore {
    return MemoryCapacityProfileStore(mutableMapOf<String, CapacityProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityProfileStore(
    var inner: CapacityProfileStore,
    var cache: TtlCache<CapacityProfile>,
    var hits: Int,
    var misses: Int,
) : CapacityProfileStore {
    override fun find(code: String): CapacityProfile? {
        val hit = (cache.get(code) as CapacityProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityProfile> = inner.list()
}

fun newCachedCapacityProfileStore(inner: CapacityProfileStore, ttl: Int, clock: Clock): CapacityProfileStore {
    return CachedCapacityProfileStore(
        inner = inner,
        cache = newTtlCache<CapacityProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity profiles. Empty fields match all. */
data class CapacityProfileQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityProfile>): MutableList<CapacityProfile> {
        val matched = filter(items) { candidate: CapacityProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityProfiles(store: CapacityProfileStore, query: CapacityProfileQuery): MutableList<CapacityProfile> {
    return query.apply(store.list())
}
