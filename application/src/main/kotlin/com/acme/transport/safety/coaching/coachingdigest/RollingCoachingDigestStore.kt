package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling coaching digests by code. */
interface RollingCoachingDigestStore {
    fun find(code: String): RollingCoachingDigest?
    fun save(item: RollingCoachingDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCoachingDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCoachingDigestStore(
    var items: MutableMap<String, RollingCoachingDigest>,
    var order: MutableList<String>,
) : RollingCoachingDigestStore {
    override fun find(code: String): RollingCoachingDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCoachingDigest) {
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

    override fun list(): MutableList<RollingCoachingDigest> {
        val out = mutableListOf<RollingCoachingDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCoachingDigestStore(): RollingCoachingDigestStore {
    return MemoryRollingCoachingDigestStore(
        mutableMapOf<String, RollingCoachingDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCoachingDigestStore(
    var inner: RollingCoachingDigestStore,
    var cache: TtlCache<RollingCoachingDigest>,
    var hits: Int,
    var misses: Int,
) : RollingCoachingDigestStore {
    override fun find(code: String): RollingCoachingDigest? {
        val hit = (cache.get(code) as RollingCoachingDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCoachingDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCoachingDigest> = inner.list()
}

fun newCachedRollingCoachingDigestStore(inner: RollingCoachingDigestStore, ttl: Int, clock: Clock): RollingCoachingDigestStore {
    return CachedRollingCoachingDigestStore(
        inner = inner,
        cache = newTtlCache<RollingCoachingDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling coaching digests. Empty fields match all. */
data class RollingCoachingDigestQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCoachingDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCoachingDigest>): MutableList<RollingCoachingDigest> {
        val matched = filter(items) { candidate: RollingCoachingDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCoachingDigests(store: RollingCoachingDigestStore, query: RollingCoachingDigestQuery): MutableList<RollingCoachingDigest> {
    return query.apply(store.list())
}
