package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul profiles by code. */
interface BackhaulProfileStore {
    fun find(code: String): BackhaulProfile?
    fun save(item: BackhaulProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulProfileStore(
    var items: MutableMap<String, BackhaulProfile>,
    var order: MutableList<String>,
) : BackhaulProfileStore {
    override fun find(code: String): BackhaulProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulProfile) {
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

    override fun list(): MutableList<BackhaulProfile> {
        val out = mutableListOf<BackhaulProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulProfileStore(): BackhaulProfileStore {
    return MemoryBackhaulProfileStore(mutableMapOf<String, BackhaulProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulProfileStore(
    var inner: BackhaulProfileStore,
    var cache: TtlCache<BackhaulProfile>,
    var hits: Int,
    var misses: Int,
) : BackhaulProfileStore {
    override fun find(code: String): BackhaulProfile? {
        val hit = (cache.get(code) as BackhaulProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulProfile> = inner.list()
}

fun newCachedBackhaulProfileStore(inner: BackhaulProfileStore, ttl: Int, clock: Clock): BackhaulProfileStore {
    return CachedBackhaulProfileStore(
        inner = inner,
        cache = newTtlCache<BackhaulProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul profiles. Empty fields match all. */
data class BackhaulProfileQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulProfile): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulProfile>): MutableList<BackhaulProfile> {
        val matched = filter(items) { candidate: BackhaulProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulProfiles(store: BackhaulProfileStore, query: BackhaulProfileQuery): MutableList<BackhaulProfile> {
    return query.apply(store.list())
}
