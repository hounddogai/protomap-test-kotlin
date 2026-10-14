package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered cross dock baselines by code. */
interface TieredCrossDockBaselineStore {
    fun find(code: String): TieredCrossDockBaseline?
    fun save(item: TieredCrossDockBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCrossDockBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCrossDockBaselineStore(
    var items: MutableMap<String, TieredCrossDockBaseline>,
    var order: MutableList<String>,
) : TieredCrossDockBaselineStore {
    override fun find(code: String): TieredCrossDockBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCrossDockBaseline) {
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

    override fun list(): MutableList<TieredCrossDockBaseline> {
        val out = mutableListOf<TieredCrossDockBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCrossDockBaselineStore(): TieredCrossDockBaselineStore {
    return MemoryTieredCrossDockBaselineStore(
        mutableMapOf<String, TieredCrossDockBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCrossDockBaselineStore(
    var inner: TieredCrossDockBaselineStore,
    var cache: TtlCache<TieredCrossDockBaseline>,
    var hits: Int,
    var misses: Int,
) : TieredCrossDockBaselineStore {
    override fun find(code: String): TieredCrossDockBaseline? {
        val hit = (cache.get(code) as TieredCrossDockBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCrossDockBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCrossDockBaseline> = inner.list()
}

fun newCachedTieredCrossDockBaselineStore(inner: TieredCrossDockBaselineStore, ttl: Int, clock: Clock): TieredCrossDockBaselineStore {
    return CachedTieredCrossDockBaselineStore(
        inner = inner,
        cache = newTtlCache<TieredCrossDockBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cross dock baselines. Empty fields match all. */
data class TieredCrossDockBaselineQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCrossDockBaseline): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCrossDockBaseline>): MutableList<TieredCrossDockBaseline> {
        val matched = filter(items) { candidate: TieredCrossDockBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCrossDockBaselines(store: TieredCrossDockBaselineStore, query: TieredCrossDockBaselineQuery): MutableList<TieredCrossDockBaseline> {
    return query.apply(store.list())
}
