package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt profiles by code. */
interface AttemptProfileStore {
    fun find(code: String): AttemptProfile?
    fun save(item: AttemptProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptProfileStore(
    var items: MutableMap<String, AttemptProfile>,
    var order: MutableList<String>,
) : AttemptProfileStore {
    override fun find(code: String): AttemptProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptProfile) {
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

    override fun list(): MutableList<AttemptProfile> {
        val out = mutableListOf<AttemptProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptProfileStore(): AttemptProfileStore {
    return MemoryAttemptProfileStore(mutableMapOf<String, AttemptProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptProfileStore(
    var inner: AttemptProfileStore,
    var cache: TtlCache<AttemptProfile>,
    var hits: Int,
    var misses: Int,
) : AttemptProfileStore {
    override fun find(code: String): AttemptProfile? {
        val hit = (cache.get(code) as AttemptProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptProfile> = inner.list()
}

fun newCachedAttemptProfileStore(inner: AttemptProfileStore, ttl: Int, clock: Clock): AttemptProfileStore {
    return CachedAttemptProfileStore(
        inner = inner,
        cache = newTtlCache<AttemptProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt profiles. Empty fields match all. */
data class AttemptProfileQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptProfile): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<AttemptProfile>): MutableList<AttemptProfile> {
        val matched = filter(items) { candidate: AttemptProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptProfiles(store: AttemptProfileStore, query: AttemptProfileQuery): MutableList<AttemptProfile> {
    return query.apply(store.list())
}
