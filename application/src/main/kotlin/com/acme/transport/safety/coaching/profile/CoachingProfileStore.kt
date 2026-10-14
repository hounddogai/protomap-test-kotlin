package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching profiles by code. */
interface CoachingProfileStore {
    fun find(code: String): CoachingProfile?
    fun save(item: CoachingProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingProfileStore(
    var items: MutableMap<String, CoachingProfile>,
    var order: MutableList<String>,
) : CoachingProfileStore {
    override fun find(code: String): CoachingProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingProfile) {
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

    override fun list(): MutableList<CoachingProfile> {
        val out = mutableListOf<CoachingProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingProfileStore(): CoachingProfileStore {
    return MemoryCoachingProfileStore(mutableMapOf<String, CoachingProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingProfileStore(
    var inner: CoachingProfileStore,
    var cache: TtlCache<CoachingProfile>,
    var hits: Int,
    var misses: Int,
) : CoachingProfileStore {
    override fun find(code: String): CoachingProfile? {
        val hit = (cache.get(code) as CoachingProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingProfile> = inner.list()
}

fun newCachedCoachingProfileStore(inner: CoachingProfileStore, ttl: Int, clock: Clock): CoachingProfileStore {
    return CachedCoachingProfileStore(
        inner = inner,
        cache = newTtlCache<CoachingProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching profiles. Empty fields match all. */
data class CoachingProfileQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingProfile>): MutableList<CoachingProfile> {
        val matched = filter(items) { candidate: CoachingProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingProfiles(store: CoachingProfileStore, query: CoachingProfileQuery): MutableList<CoachingProfile> {
    return query.apply(store.list())
}
