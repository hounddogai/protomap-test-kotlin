package com.acme.transport.safety.violation.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling violation profiles by code. */
interface RollingViolationProfileStore {
    fun find(code: String): RollingViolationProfile?
    fun save(item: RollingViolationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingViolationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingViolationProfileStore(
    var items: MutableMap<String, RollingViolationProfile>,
    var order: MutableList<String>,
) : RollingViolationProfileStore {
    override fun find(code: String): RollingViolationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingViolationProfile) {
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

    override fun list(): MutableList<RollingViolationProfile> {
        val out = mutableListOf<RollingViolationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingViolationProfileStore(): RollingViolationProfileStore {
    return MemoryRollingViolationProfileStore(
        mutableMapOf<String, RollingViolationProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingViolationProfileStore(
    var inner: RollingViolationProfileStore,
    var cache: TtlCache<RollingViolationProfile>,
    var hits: Int,
    var misses: Int,
) : RollingViolationProfileStore {
    override fun find(code: String): RollingViolationProfile? {
        val hit = (cache.get(code) as RollingViolationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingViolationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingViolationProfile> = inner.list()
}

fun newCachedRollingViolationProfileStore(inner: RollingViolationProfileStore, ttl: Int, clock: Clock): RollingViolationProfileStore {
    return CachedRollingViolationProfileStore(
        inner = inner,
        cache = newTtlCache<RollingViolationProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling violation profiles. Empty fields match all. */
data class RollingViolationProfileQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingViolationProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingViolationProfile>): MutableList<RollingViolationProfile> {
        val matched = filter(items) { candidate: RollingViolationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingViolationProfiles(store: RollingViolationProfileStore, query: RollingViolationProfileQuery): MutableList<RollingViolationProfile> {
    return query.apply(store.list())
}
