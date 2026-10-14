package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual near miss profiles by code. */
interface ManualNearMissProfileStore {
    fun find(code: String): ManualNearMissProfile?
    fun save(item: ManualNearMissProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualNearMissProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualNearMissProfileStore(
    var items: MutableMap<String, ManualNearMissProfile>,
    var order: MutableList<String>,
) : ManualNearMissProfileStore {
    override fun find(code: String): ManualNearMissProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualNearMissProfile) {
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

    override fun list(): MutableList<ManualNearMissProfile> {
        val out = mutableListOf<ManualNearMissProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualNearMissProfileStore(): ManualNearMissProfileStore {
    return MemoryManualNearMissProfileStore(
        mutableMapOf<String, ManualNearMissProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualNearMissProfileStore(
    var inner: ManualNearMissProfileStore,
    var cache: TtlCache<ManualNearMissProfile>,
    var hits: Int,
    var misses: Int,
) : ManualNearMissProfileStore {
    override fun find(code: String): ManualNearMissProfile? {
        val hit = (cache.get(code) as ManualNearMissProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualNearMissProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualNearMissProfile> = inner.list()
}

fun newCachedManualNearMissProfileStore(inner: ManualNearMissProfileStore, ttl: Int, clock: Clock): ManualNearMissProfileStore {
    return CachedManualNearMissProfileStore(
        inner = inner,
        cache = newTtlCache<ManualNearMissProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual near miss profiles. Empty fields match all. */
data class ManualNearMissProfileQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualNearMissProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualNearMissProfile>): MutableList<ManualNearMissProfile> {
        val matched = filter(items) { candidate: ManualNearMissProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualNearMissProfiles(store: ManualNearMissProfileStore, query: ManualNearMissProfileQuery): MutableList<ManualNearMissProfile> {
    return query.apply(store.list())
}
