package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul scores by code. */
interface BackhaulScoreStore {
    fun find(code: String): BackhaulScore?
    fun save(item: BackhaulScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulScoreStore(
    var items: MutableMap<String, BackhaulScore>,
    var order: MutableList<String>,
) : BackhaulScoreStore {
    override fun find(code: String): BackhaulScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulScore) {
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

    override fun list(): MutableList<BackhaulScore> {
        val out = mutableListOf<BackhaulScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulScoreStore(): BackhaulScoreStore {
    return MemoryBackhaulScoreStore(mutableMapOf<String, BackhaulScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulScoreStore(
    var inner: BackhaulScoreStore,
    var cache: TtlCache<BackhaulScore>,
    var hits: Int,
    var misses: Int,
) : BackhaulScoreStore {
    override fun find(code: String): BackhaulScore? {
        val hit = (cache.get(code) as BackhaulScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulScore> = inner.list()
}

fun newCachedBackhaulScoreStore(inner: BackhaulScoreStore, ttl: Int, clock: Clock): BackhaulScoreStore {
    return CachedBackhaulScoreStore(
        inner = inner,
        cache = newTtlCache<BackhaulScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul scores. Empty fields match all. */
data class BackhaulScoreQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulScore): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulScore>): MutableList<BackhaulScore> {
        val matched = filter(items) { candidate: BackhaulScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulScores(store: BackhaulScoreStore, query: BackhaulScoreQuery): MutableList<BackhaulScore> {
    return query.apply(store.list())
}
