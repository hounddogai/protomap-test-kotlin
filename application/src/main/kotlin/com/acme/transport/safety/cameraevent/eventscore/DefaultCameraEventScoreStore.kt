package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default camera event scores by code. */
interface DefaultCameraEventScoreStore {
    fun find(code: String): DefaultCameraEventScore?
    fun save(item: DefaultCameraEventScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCameraEventScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCameraEventScoreStore(
    var items: MutableMap<String, DefaultCameraEventScore>,
    var order: MutableList<String>,
) : DefaultCameraEventScoreStore {
    override fun find(code: String): DefaultCameraEventScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCameraEventScore) {
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

    override fun list(): MutableList<DefaultCameraEventScore> {
        val out = mutableListOf<DefaultCameraEventScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCameraEventScoreStore(): DefaultCameraEventScoreStore {
    return MemoryDefaultCameraEventScoreStore(
        mutableMapOf<String, DefaultCameraEventScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCameraEventScoreStore(
    var inner: DefaultCameraEventScoreStore,
    var cache: TtlCache<DefaultCameraEventScore>,
    var hits: Int,
    var misses: Int,
) : DefaultCameraEventScoreStore {
    override fun find(code: String): DefaultCameraEventScore? {
        val hit = (cache.get(code) as DefaultCameraEventScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCameraEventScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCameraEventScore> = inner.list()
}

fun newCachedDefaultCameraEventScoreStore(inner: DefaultCameraEventScoreStore, ttl: Int, clock: Clock): DefaultCameraEventScoreStore {
    return CachedDefaultCameraEventScoreStore(
        inner = inner,
        cache = newTtlCache<DefaultCameraEventScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default camera event scores. Empty fields match all. */
data class DefaultCameraEventScoreQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCameraEventScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefaultCameraEventScore>): MutableList<DefaultCameraEventScore> {
        val matched = filter(items) { candidate: DefaultCameraEventScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCameraEventScores(store: DefaultCameraEventScoreStore, query: DefaultCameraEventScoreQuery): MutableList<DefaultCameraEventScore> {
    return query.apply(store.list())
}
