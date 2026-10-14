package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient cross dock baselines by code. */
interface LenientCrossDockBaselineStore {
    fun find(code: String): LenientCrossDockBaseline?
    fun save(item: LenientCrossDockBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCrossDockBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCrossDockBaselineStore(
    var items: MutableMap<String, LenientCrossDockBaseline>,
    var order: MutableList<String>,
) : LenientCrossDockBaselineStore {
    override fun find(code: String): LenientCrossDockBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCrossDockBaseline) {
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

    override fun list(): MutableList<LenientCrossDockBaseline> {
        val out = mutableListOf<LenientCrossDockBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCrossDockBaselineStore(): LenientCrossDockBaselineStore {
    return MemoryLenientCrossDockBaselineStore(
        mutableMapOf<String, LenientCrossDockBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCrossDockBaselineStore(
    var inner: LenientCrossDockBaselineStore,
    var cache: TtlCache<LenientCrossDockBaseline>,
    var hits: Int,
    var misses: Int,
) : LenientCrossDockBaselineStore {
    override fun find(code: String): LenientCrossDockBaseline? {
        val hit = (cache.get(code) as LenientCrossDockBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCrossDockBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCrossDockBaseline> = inner.list()
}

fun newCachedLenientCrossDockBaselineStore(inner: LenientCrossDockBaselineStore, ttl: Int, clock: Clock): LenientCrossDockBaselineStore {
    return CachedLenientCrossDockBaselineStore(
        inner = inner,
        cache = newTtlCache<LenientCrossDockBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cross dock baselines. Empty fields match all. */
data class LenientCrossDockBaselineQuery(
    var lane: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCrossDockBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCrossDockBaseline>): MutableList<LenientCrossDockBaseline> {
        val matched = filter(items) { candidate: LenientCrossDockBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCrossDockBaselines(store: LenientCrossDockBaselineStore, query: LenientCrossDockBaselineQuery): MutableList<LenientCrossDockBaseline> {
    return query.apply(store.list())
}
