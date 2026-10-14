package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy parcel scores by code. */
interface LegacyParcelScoreStore {
    fun find(code: String): LegacyParcelScore?
    fun save(item: LegacyParcelScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyParcelScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyParcelScoreStore(
    var items: MutableMap<String, LegacyParcelScore>,
    var order: MutableList<String>,
) : LegacyParcelScoreStore {
    override fun find(code: String): LegacyParcelScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyParcelScore) {
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

    override fun list(): MutableList<LegacyParcelScore> {
        val out = mutableListOf<LegacyParcelScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyParcelScoreStore(): LegacyParcelScoreStore {
    return MemoryLegacyParcelScoreStore(mutableMapOf<String, LegacyParcelScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyParcelScoreStore(
    var inner: LegacyParcelScoreStore,
    var cache: TtlCache<LegacyParcelScore>,
    var hits: Int,
    var misses: Int,
) : LegacyParcelScoreStore {
    override fun find(code: String): LegacyParcelScore? {
        val hit = (cache.get(code) as LegacyParcelScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyParcelScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyParcelScore> = inner.list()
}

fun newCachedLegacyParcelScoreStore(inner: LegacyParcelScoreStore, ttl: Int, clock: Clock): LegacyParcelScoreStore {
    return CachedLegacyParcelScoreStore(
        inner = inner,
        cache = newTtlCache<LegacyParcelScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy parcel scores. Empty fields match all. */
data class LegacyParcelScoreQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyParcelScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LegacyParcelScore>): MutableList<LegacyParcelScore> {
        val matched = filter(items) { candidate: LegacyParcelScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyParcelScores(store: LegacyParcelScoreStore, query: LegacyParcelScoreQuery): MutableList<LegacyParcelScore> {
    return query.apply(store.list())
}
