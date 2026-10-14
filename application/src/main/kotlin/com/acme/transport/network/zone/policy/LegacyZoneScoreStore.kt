package com.acme.transport.network.zone.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy zone scores by code. */
interface LegacyZoneScoreStore {
    fun find(code: String): LegacyZoneScore?
    fun save(item: LegacyZoneScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyZoneScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyZoneScoreStore(
    var items: MutableMap<String, LegacyZoneScore>,
    var order: MutableList<String>,
) : LegacyZoneScoreStore {
    override fun find(code: String): LegacyZoneScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyZoneScore) {
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

    override fun list(): MutableList<LegacyZoneScore> {
        val out = mutableListOf<LegacyZoneScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyZoneScoreStore(): LegacyZoneScoreStore {
    return MemoryLegacyZoneScoreStore(mutableMapOf<String, LegacyZoneScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyZoneScoreStore(
    var inner: LegacyZoneScoreStore,
    var cache: TtlCache<LegacyZoneScore>,
    var hits: Int,
    var misses: Int,
) : LegacyZoneScoreStore {
    override fun find(code: String): LegacyZoneScore? {
        val hit = (cache.get(code) as LegacyZoneScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyZoneScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyZoneScore> = inner.list()
}

fun newCachedLegacyZoneScoreStore(inner: LegacyZoneScoreStore, ttl: Int, clock: Clock): LegacyZoneScoreStore {
    return CachedLegacyZoneScoreStore(
        inner = inner,
        cache = newTtlCache<LegacyZoneScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy zone scores. Empty fields match all. */
data class LegacyZoneScoreQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyZoneScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyZoneScore>): MutableList<LegacyZoneScore> {
        val matched = filter(items) { candidate: LegacyZoneScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyZoneScores(store: LegacyZoneScoreStore, query: LegacyZoneScoreQuery): MutableList<LegacyZoneScore> {
    return query.apply(store.list())
}
