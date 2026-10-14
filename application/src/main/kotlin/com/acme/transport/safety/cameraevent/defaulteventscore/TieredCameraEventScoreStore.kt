package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered camera event scores by code. */
interface TieredCameraEventScoreStore {
    fun find(code: String): TieredCameraEventScore?
    fun save(item: TieredCameraEventScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCameraEventScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCameraEventScoreStore(
    var items: MutableMap<String, TieredCameraEventScore>,
    var order: MutableList<String>,
) : TieredCameraEventScoreStore {
    override fun find(code: String): TieredCameraEventScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCameraEventScore) {
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

    override fun list(): MutableList<TieredCameraEventScore> {
        val out = mutableListOf<TieredCameraEventScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCameraEventScoreStore(): TieredCameraEventScoreStore {
    return MemoryTieredCameraEventScoreStore(
        mutableMapOf<String, TieredCameraEventScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCameraEventScoreStore(
    var inner: TieredCameraEventScoreStore,
    var cache: TtlCache<TieredCameraEventScore>,
    var hits: Int,
    var misses: Int,
) : TieredCameraEventScoreStore {
    override fun find(code: String): TieredCameraEventScore? {
        val hit = (cache.get(code) as TieredCameraEventScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCameraEventScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCameraEventScore> = inner.list()
}

fun newCachedTieredCameraEventScoreStore(inner: TieredCameraEventScoreStore, ttl: Int, clock: Clock): TieredCameraEventScoreStore {
    return CachedTieredCameraEventScoreStore(
        inner = inner,
        cache = newTtlCache<TieredCameraEventScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered camera event scores. Empty fields match all. */
data class TieredCameraEventScoreQuery(
    var channel: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCameraEventScore): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCameraEventScore>): MutableList<TieredCameraEventScore> {
        val matched = filter(items) { candidate: TieredCameraEventScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCameraEventScores(store: TieredCameraEventScoreStore, query: TieredCameraEventScoreQuery): MutableList<TieredCameraEventScore> {
    return query.apply(store.list())
}
