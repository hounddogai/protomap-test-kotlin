package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled service area scores by code. */
interface ScheduledServiceAreaScoreStore {
    fun find(code: String): ScheduledServiceAreaScore?
    fun save(item: ScheduledServiceAreaScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledServiceAreaScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledServiceAreaScoreStore(
    var items: MutableMap<String, ScheduledServiceAreaScore>,
    var order: MutableList<String>,
) : ScheduledServiceAreaScoreStore {
    override fun find(code: String): ScheduledServiceAreaScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledServiceAreaScore) {
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

    override fun list(): MutableList<ScheduledServiceAreaScore> {
        val out = mutableListOf<ScheduledServiceAreaScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledServiceAreaScoreStore(): ScheduledServiceAreaScoreStore {
    return MemoryScheduledServiceAreaScoreStore(
        mutableMapOf<String, ScheduledServiceAreaScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledServiceAreaScoreStore(
    var inner: ScheduledServiceAreaScoreStore,
    var cache: TtlCache<ScheduledServiceAreaScore>,
    var hits: Int,
    var misses: Int,
) : ScheduledServiceAreaScoreStore {
    override fun find(code: String): ScheduledServiceAreaScore? {
        val hit = (cache.get(code) as ScheduledServiceAreaScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledServiceAreaScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledServiceAreaScore> = inner.list()
}

fun newCachedScheduledServiceAreaScoreStore(inner: ScheduledServiceAreaScoreStore, ttl: Int, clock: Clock): ScheduledServiceAreaScoreStore {
    return CachedScheduledServiceAreaScoreStore(
        inner = inner,
        cache = newTtlCache<ScheduledServiceAreaScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled service area scores. Empty fields match all. */
data class ScheduledServiceAreaScoreQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledServiceAreaScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledServiceAreaScore>): MutableList<ScheduledServiceAreaScore> {
        val matched = filter(items) { candidate: ScheduledServiceAreaScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledServiceAreaScores(store: ScheduledServiceAreaScoreStore, query: ScheduledServiceAreaScoreQuery): MutableList<ScheduledServiceAreaScore> {
    return query.apply(store.list())
}
