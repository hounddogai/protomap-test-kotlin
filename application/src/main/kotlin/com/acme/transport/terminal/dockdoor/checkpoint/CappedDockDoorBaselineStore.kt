package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dock door baselines by code. */
interface CappedDockDoorBaselineStore {
    fun find(code: String): CappedDockDoorBaseline?
    fun save(item: CappedDockDoorBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDockDoorBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDockDoorBaselineStore(
    var items: MutableMap<String, CappedDockDoorBaseline>,
    var order: MutableList<String>,
) : CappedDockDoorBaselineStore {
    override fun find(code: String): CappedDockDoorBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDockDoorBaseline) {
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

    override fun list(): MutableList<CappedDockDoorBaseline> {
        val out = mutableListOf<CappedDockDoorBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDockDoorBaselineStore(): CappedDockDoorBaselineStore {
    return MemoryCappedDockDoorBaselineStore(
        mutableMapOf<String, CappedDockDoorBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDockDoorBaselineStore(
    var inner: CappedDockDoorBaselineStore,
    var cache: TtlCache<CappedDockDoorBaseline>,
    var hits: Int,
    var misses: Int,
) : CappedDockDoorBaselineStore {
    override fun find(code: String): CappedDockDoorBaseline? {
        val hit = (cache.get(code) as CappedDockDoorBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDockDoorBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDockDoorBaseline> = inner.list()
}

fun newCachedCappedDockDoorBaselineStore(inner: CappedDockDoorBaselineStore, ttl: Int, clock: Clock): CappedDockDoorBaselineStore {
    return CachedCappedDockDoorBaselineStore(
        inner = inner,
        cache = newTtlCache<CappedDockDoorBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dock door baselines. Empty fields match all. */
data class CappedDockDoorBaselineQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDockDoorBaseline): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDockDoorBaseline>): MutableList<CappedDockDoorBaseline> {
        val matched = filter(items) { candidate: CappedDockDoorBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDockDoorBaselines(store: CappedDockDoorBaselineStore, query: CappedDockDoorBaselineQuery): MutableList<CappedDockDoorBaseline> {
    return query.apply(store.list())
}
