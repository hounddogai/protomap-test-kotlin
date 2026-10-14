package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending cross dock scores by code. */
interface PendingCrossDockScoreStore {
    fun find(code: String): PendingCrossDockScore?
    fun save(item: PendingCrossDockScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCrossDockScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCrossDockScoreStore(
    var items: MutableMap<String, PendingCrossDockScore>,
    var order: MutableList<String>,
) : PendingCrossDockScoreStore {
    override fun find(code: String): PendingCrossDockScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCrossDockScore) {
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

    override fun list(): MutableList<PendingCrossDockScore> {
        val out = mutableListOf<PendingCrossDockScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCrossDockScoreStore(): PendingCrossDockScoreStore {
    return MemoryPendingCrossDockScoreStore(
        mutableMapOf<String, PendingCrossDockScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCrossDockScoreStore(
    var inner: PendingCrossDockScoreStore,
    var cache: TtlCache<PendingCrossDockScore>,
    var hits: Int,
    var misses: Int,
) : PendingCrossDockScoreStore {
    override fun find(code: String): PendingCrossDockScore? {
        val hit = (cache.get(code) as PendingCrossDockScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCrossDockScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCrossDockScore> = inner.list()
}

fun newCachedPendingCrossDockScoreStore(inner: PendingCrossDockScoreStore, ttl: Int, clock: Clock): PendingCrossDockScoreStore {
    return CachedPendingCrossDockScoreStore(
        inner = inner,
        cache = newTtlCache<PendingCrossDockScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending cross dock scores. Empty fields match all. */
data class PendingCrossDockScoreQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCrossDockScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PendingCrossDockScore>): MutableList<PendingCrossDockScore> {
        val matched = filter(items) { candidate: PendingCrossDockScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCrossDockScores(store: PendingCrossDockScoreStore, query: PendingCrossDockScoreQuery): MutableList<PendingCrossDockScore> {
    return query.apply(store.list())
}
