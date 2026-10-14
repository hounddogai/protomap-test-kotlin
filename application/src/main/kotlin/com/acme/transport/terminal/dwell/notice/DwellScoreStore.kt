package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell scores by code. */
interface DwellScoreStore {
    fun find(code: String): DwellScore?
    fun save(item: DwellScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellScoreStore(
    var items: MutableMap<String, DwellScore>,
    var order: MutableList<String>,
) : DwellScoreStore {
    override fun find(code: String): DwellScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellScore) {
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

    override fun list(): MutableList<DwellScore> {
        val out = mutableListOf<DwellScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellScoreStore(): DwellScoreStore {
    return MemoryDwellScoreStore(mutableMapOf<String, DwellScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellScoreStore(
    var inner: DwellScoreStore,
    var cache: TtlCache<DwellScore>,
    var hits: Int,
    var misses: Int,
) : DwellScoreStore {
    override fun find(code: String): DwellScore? {
        val hit = (cache.get(code) as DwellScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellScore> = inner.list()
}

fun newCachedDwellScoreStore(inner: DwellScoreStore, ttl: Int, clock: Clock): DwellScoreStore {
    return CachedDwellScoreStore(
        inner = inner,
        cache = newTtlCache<DwellScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell scores. Empty fields match all. */
data class DwellScoreQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellScore>): MutableList<DwellScore> {
        val matched = filter(items) { candidate: DwellScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellScores(store: DwellScoreStore, query: DwellScoreQuery): MutableList<DwellScore> {
    return query.apply(store.list())
}
