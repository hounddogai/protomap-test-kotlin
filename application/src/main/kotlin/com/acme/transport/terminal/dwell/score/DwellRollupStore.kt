package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell rollups by code. */
interface DwellRollupStore {
    fun find(code: String): DwellRollup?
    fun save(item: DwellRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellRollupStore(
    var items: MutableMap<String, DwellRollup>,
    var order: MutableList<String>,
) : DwellRollupStore {
    override fun find(code: String): DwellRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellRollup) {
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

    override fun list(): MutableList<DwellRollup> {
        val out = mutableListOf<DwellRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellRollupStore(): DwellRollupStore {
    return MemoryDwellRollupStore(mutableMapOf<String, DwellRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellRollupStore(
    var inner: DwellRollupStore,
    var cache: TtlCache<DwellRollup>,
    var hits: Int,
    var misses: Int,
) : DwellRollupStore {
    override fun find(code: String): DwellRollup? {
        val hit = (cache.get(code) as DwellRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellRollup> = inner.list()
}

fun newCachedDwellRollupStore(inner: DwellRollupStore, ttl: Int, clock: Clock): DwellRollupStore {
    return CachedDwellRollupStore(
        inner = inner,
        cache = newTtlCache<DwellRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell rollups. Empty fields match all. */
data class DwellRollupQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellRollup>): MutableList<DwellRollup> {
        val matched = filter(items) { candidate: DwellRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellRollups(store: DwellRollupStore, query: DwellRollupQuery): MutableList<DwellRollup> {
    return query.apply(store.list())
}
