package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss profiles by code. */
interface NearMissProfileStore {
    fun find(code: String): NearMissProfile?
    fun save(item: NearMissProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissProfileStore(
    var items: MutableMap<String, NearMissProfile>,
    var order: MutableList<String>,
) : NearMissProfileStore {
    override fun find(code: String): NearMissProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissProfile) {
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

    override fun list(): MutableList<NearMissProfile> {
        val out = mutableListOf<NearMissProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissProfileStore(): NearMissProfileStore {
    return MemoryNearMissProfileStore(mutableMapOf<String, NearMissProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissProfileStore(
    var inner: NearMissProfileStore,
    var cache: TtlCache<NearMissProfile>,
    var hits: Int,
    var misses: Int,
) : NearMissProfileStore {
    override fun find(code: String): NearMissProfile? {
        val hit = (cache.get(code) as NearMissProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissProfile> = inner.list()
}

fun newCachedNearMissProfileStore(inner: NearMissProfileStore, ttl: Int, clock: Clock): NearMissProfileStore {
    return CachedNearMissProfileStore(
        inner = inner,
        cache = newTtlCache<NearMissProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss profiles. Empty fields match all. */
data class NearMissProfileQuery(
    var region: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissProfile>): MutableList<NearMissProfile> {
        val matched = filter(items) { candidate: NearMissProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissProfiles(store: NearMissProfileStore, query: NearMissProfileQuery): MutableList<NearMissProfile> {
    return query.apply(store.list())
}
