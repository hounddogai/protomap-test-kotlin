package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel scores by code. */
interface ParcelScoreStore {
    fun find(code: String): ParcelScore?
    fun save(item: ParcelScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelScoreStore(
    var items: MutableMap<String, ParcelScore>,
    var order: MutableList<String>,
) : ParcelScoreStore {
    override fun find(code: String): ParcelScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelScore) {
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

    override fun list(): MutableList<ParcelScore> {
        val out = mutableListOf<ParcelScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelScoreStore(): ParcelScoreStore {
    return MemoryParcelScoreStore(mutableMapOf<String, ParcelScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelScoreStore(
    var inner: ParcelScoreStore,
    var cache: TtlCache<ParcelScore>,
    var hits: Int,
    var misses: Int,
) : ParcelScoreStore {
    override fun find(code: String): ParcelScore? {
        val hit = (cache.get(code) as ParcelScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelScore> = inner.list()
}

fun newCachedParcelScoreStore(inner: ParcelScoreStore, ttl: Int, clock: Clock): ParcelScoreStore {
    return CachedParcelScoreStore(
        inner = inner,
        cache = newTtlCache<ParcelScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel scores. Empty fields match all. */
data class ParcelScoreQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelScore): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelScore>): MutableList<ParcelScore> {
        val matched = filter(items) { candidate: ParcelScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelScores(store: ParcelScoreStore, query: ParcelScoreQuery): MutableList<ParcelScore> {
    return query.apply(store.list())
}
