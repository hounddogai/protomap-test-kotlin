package com.acme.transport.customs.hold.assignment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped hold rollups by code. */
interface CappedHoldRollupStore {
    fun find(code: String): CappedHoldRollup?
    fun save(item: CappedHoldRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedHoldRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedHoldRollupStore(
    var items: MutableMap<String, CappedHoldRollup>,
    var order: MutableList<String>,
) : CappedHoldRollupStore {
    override fun find(code: String): CappedHoldRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedHoldRollup) {
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

    override fun list(): MutableList<CappedHoldRollup> {
        val out = mutableListOf<CappedHoldRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedHoldRollupStore(): CappedHoldRollupStore {
    return MemoryCappedHoldRollupStore(mutableMapOf<String, CappedHoldRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedHoldRollupStore(
    var inner: CappedHoldRollupStore,
    var cache: TtlCache<CappedHoldRollup>,
    var hits: Int,
    var misses: Int,
) : CappedHoldRollupStore {
    override fun find(code: String): CappedHoldRollup? {
        val hit = (cache.get(code) as CappedHoldRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedHoldRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedHoldRollup> = inner.list()
}

fun newCachedCappedHoldRollupStore(inner: CappedHoldRollupStore, ttl: Int, clock: Clock): CappedHoldRollupStore {
    return CachedCappedHoldRollupStore(
        inner = inner,
        cache = newTtlCache<CappedHoldRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped hold rollups. Empty fields match all. */
data class CappedHoldRollupQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedHoldRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedHoldRollup>): MutableList<CappedHoldRollup> {
        val matched = filter(items) { candidate: CappedHoldRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedHoldRollups(store: CappedHoldRollupStore, query: CappedHoldRollupQuery): MutableList<CappedHoldRollup> {
    return query.apply(store.list())
}
