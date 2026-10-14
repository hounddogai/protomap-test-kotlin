package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dock door scores by code. */
interface StrictDockDoorScoreStore {
    fun find(code: String): StrictDockDoorScore?
    fun save(item: StrictDockDoorScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDockDoorScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDockDoorScoreStore(
    var items: MutableMap<String, StrictDockDoorScore>,
    var order: MutableList<String>,
) : StrictDockDoorScoreStore {
    override fun find(code: String): StrictDockDoorScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDockDoorScore) {
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

    override fun list(): MutableList<StrictDockDoorScore> {
        val out = mutableListOf<StrictDockDoorScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDockDoorScoreStore(): StrictDockDoorScoreStore {
    return MemoryStrictDockDoorScoreStore(mutableMapOf<String, StrictDockDoorScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDockDoorScoreStore(
    var inner: StrictDockDoorScoreStore,
    var cache: TtlCache<StrictDockDoorScore>,
    var hits: Int,
    var misses: Int,
) : StrictDockDoorScoreStore {
    override fun find(code: String): StrictDockDoorScore? {
        val hit = (cache.get(code) as StrictDockDoorScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDockDoorScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDockDoorScore> = inner.list()
}

fun newCachedStrictDockDoorScoreStore(inner: StrictDockDoorScoreStore, ttl: Int, clock: Clock): StrictDockDoorScoreStore {
    return CachedStrictDockDoorScoreStore(
        inner = inner,
        cache = newTtlCache<StrictDockDoorScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dock door scores. Empty fields match all. */
data class StrictDockDoorScoreQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDockDoorScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDockDoorScore>): MutableList<StrictDockDoorScore> {
        val matched = filter(items) { candidate: StrictDockDoorScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDockDoorScores(store: StrictDockDoorScoreStore, query: StrictDockDoorScoreQuery): MutableList<StrictDockDoorScore> {
    return query.apply(store.list())
}
