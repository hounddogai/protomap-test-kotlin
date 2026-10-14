package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event scores by code. */
interface CameraEventScoreStore {
    fun find(code: String): CameraEventScore?
    fun save(item: CameraEventScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventScoreStore(
    var items: MutableMap<String, CameraEventScore>,
    var order: MutableList<String>,
) : CameraEventScoreStore {
    override fun find(code: String): CameraEventScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventScore) {
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

    override fun list(): MutableList<CameraEventScore> {
        val out = mutableListOf<CameraEventScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventScoreStore(): CameraEventScoreStore {
    return MemoryCameraEventScoreStore(mutableMapOf<String, CameraEventScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventScoreStore(
    var inner: CameraEventScoreStore,
    var cache: TtlCache<CameraEventScore>,
    var hits: Int,
    var misses: Int,
) : CameraEventScoreStore {
    override fun find(code: String): CameraEventScore? {
        val hit = (cache.get(code) as CameraEventScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventScore> = inner.list()
}

fun newCachedCameraEventScoreStore(inner: CameraEventScoreStore, ttl: Int, clock: Clock): CameraEventScoreStore {
    return CachedCameraEventScoreStore(
        inner = inner,
        cache = newTtlCache<CameraEventScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event scores. Empty fields match all. */
data class CameraEventScoreQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CameraEventScore>): MutableList<CameraEventScore> {
        val matched = filter(items) { candidate: CameraEventScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventScores(store: CameraEventScoreStore, query: CameraEventScoreQuery): MutableList<CameraEventScore> {
    return query.apply(store.list())
}
