package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock scores by code. */
interface CrossDockScoreStore {
    fun find(code: String): CrossDockScore?
    fun save(item: CrossDockScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockScoreStore(
    var items: MutableMap<String, CrossDockScore>,
    var order: MutableList<String>,
) : CrossDockScoreStore {
    override fun find(code: String): CrossDockScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockScore) {
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

    override fun list(): MutableList<CrossDockScore> {
        val out = mutableListOf<CrossDockScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockScoreStore(): CrossDockScoreStore {
    return MemoryCrossDockScoreStore(mutableMapOf<String, CrossDockScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockScoreStore(
    var inner: CrossDockScoreStore,
    var cache: TtlCache<CrossDockScore>,
    var hits: Int,
    var misses: Int,
) : CrossDockScoreStore {
    override fun find(code: String): CrossDockScore? {
        val hit = (cache.get(code) as CrossDockScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockScore> = inner.list()
}

fun newCachedCrossDockScoreStore(inner: CrossDockScoreStore, ttl: Int, clock: Clock): CrossDockScoreStore {
    return CachedCrossDockScoreStore(
        inner = inner,
        cache = newTtlCache<CrossDockScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock scores. Empty fields match all. */
data class CrossDockScoreQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockScore>): MutableList<CrossDockScore> {
        val matched = filter(items) { candidate: CrossDockScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockScores(store: CrossDockScoreStore, query: CrossDockScoreQuery): MutableList<CrossDockScore> {
    return query.apply(store.list())
}
