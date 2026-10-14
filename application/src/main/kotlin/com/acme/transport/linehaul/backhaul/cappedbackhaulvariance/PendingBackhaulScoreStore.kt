package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending backhaul scores by code. */
interface PendingBackhaulScoreStore {
    fun find(code: String): PendingBackhaulScore?
    fun save(item: PendingBackhaulScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBackhaulScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBackhaulScoreStore(
    var items: MutableMap<String, PendingBackhaulScore>,
    var order: MutableList<String>,
) : PendingBackhaulScoreStore {
    override fun find(code: String): PendingBackhaulScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBackhaulScore) {
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

    override fun list(): MutableList<PendingBackhaulScore> {
        val out = mutableListOf<PendingBackhaulScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBackhaulScoreStore(): PendingBackhaulScoreStore {
    return MemoryPendingBackhaulScoreStore(
        mutableMapOf<String, PendingBackhaulScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBackhaulScoreStore(
    var inner: PendingBackhaulScoreStore,
    var cache: TtlCache<PendingBackhaulScore>,
    var hits: Int,
    var misses: Int,
) : PendingBackhaulScoreStore {
    override fun find(code: String): PendingBackhaulScore? {
        val hit = (cache.get(code) as PendingBackhaulScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBackhaulScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBackhaulScore> = inner.list()
}

fun newCachedPendingBackhaulScoreStore(inner: PendingBackhaulScoreStore, ttl: Int, clock: Clock): PendingBackhaulScoreStore {
    return CachedPendingBackhaulScoreStore(
        inner = inner,
        cache = newTtlCache<PendingBackhaulScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending backhaul scores. Empty fields match all. */
data class PendingBackhaulScoreQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBackhaulScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingBackhaulScore>): MutableList<PendingBackhaulScore> {
        val matched = filter(items) { candidate: PendingBackhaulScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBackhaulScores(store: PendingBackhaulScoreStore, query: PendingBackhaulScoreQuery): MutableList<PendingBackhaulScore> {
    return query.apply(store.list())
}
