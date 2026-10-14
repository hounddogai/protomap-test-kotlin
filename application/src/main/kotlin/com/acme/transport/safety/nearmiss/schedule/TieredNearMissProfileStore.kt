package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered near miss profiles by code. */
interface TieredNearMissProfileStore {
    fun find(code: String): TieredNearMissProfile?
    fun save(item: TieredNearMissProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredNearMissProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredNearMissProfileStore(
    var items: MutableMap<String, TieredNearMissProfile>,
    var order: MutableList<String>,
) : TieredNearMissProfileStore {
    override fun find(code: String): TieredNearMissProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredNearMissProfile) {
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

    override fun list(): MutableList<TieredNearMissProfile> {
        val out = mutableListOf<TieredNearMissProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredNearMissProfileStore(): TieredNearMissProfileStore {
    return MemoryTieredNearMissProfileStore(
        mutableMapOf<String, TieredNearMissProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredNearMissProfileStore(
    var inner: TieredNearMissProfileStore,
    var cache: TtlCache<TieredNearMissProfile>,
    var hits: Int,
    var misses: Int,
) : TieredNearMissProfileStore {
    override fun find(code: String): TieredNearMissProfile? {
        val hit = (cache.get(code) as TieredNearMissProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredNearMissProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredNearMissProfile> = inner.list()
}

fun newCachedTieredNearMissProfileStore(inner: TieredNearMissProfileStore, ttl: Int, clock: Clock): TieredNearMissProfileStore {
    return CachedTieredNearMissProfileStore(
        inner = inner,
        cache = newTtlCache<TieredNearMissProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered near miss profiles. Empty fields match all. */
data class TieredNearMissProfileQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredNearMissProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredNearMissProfile>): MutableList<TieredNearMissProfile> {
        val matched = filter(items) { candidate: TieredNearMissProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredNearMissProfiles(store: TieredNearMissProfileStore, query: TieredNearMissProfileQuery): MutableList<TieredNearMissProfile> {
    return query.apply(store.list())
}
