package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone scores by code. */
interface ZoneScoreStore {
    fun find(code: String): ZoneScore?
    fun save(item: ZoneScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneScoreStore(
    var items: MutableMap<String, ZoneScore>,
    var order: MutableList<String>,
) : ZoneScoreStore {
    override fun find(code: String): ZoneScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneScore) {
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

    override fun list(): MutableList<ZoneScore> {
        val out = mutableListOf<ZoneScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneScoreStore(): ZoneScoreStore {
    return MemoryZoneScoreStore(mutableMapOf<String, ZoneScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneScoreStore(
    var inner: ZoneScoreStore,
    var cache: TtlCache<ZoneScore>,
    var hits: Int,
    var misses: Int,
) : ZoneScoreStore {
    override fun find(code: String): ZoneScore? {
        val hit = (cache.get(code) as ZoneScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneScore> = inner.list()
}

fun newCachedZoneScoreStore(inner: ZoneScoreStore, ttl: Int, clock: Clock): ZoneScoreStore {
    return CachedZoneScoreStore(
        inner = inner,
        cache = newTtlCache<ZoneScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone scores. Empty fields match all. */
data class ZoneScoreQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneScore): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<ZoneScore>): MutableList<ZoneScore> {
        val matched = filter(items) { candidate: ZoneScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneScores(store: ZoneScoreStore, query: ZoneScoreQuery): MutableList<ZoneScore> {
    return query.apply(store.list())
}
