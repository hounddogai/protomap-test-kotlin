package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending dwell rollups by code. */
interface PendingDwellRollupStore {
    fun find(code: String): PendingDwellRollup?
    fun save(item: PendingDwellRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDwellRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDwellRollupStore(
    var items: MutableMap<String, PendingDwellRollup>,
    var order: MutableList<String>,
) : PendingDwellRollupStore {
    override fun find(code: String): PendingDwellRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDwellRollup) {
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

    override fun list(): MutableList<PendingDwellRollup> {
        val out = mutableListOf<PendingDwellRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDwellRollupStore(): PendingDwellRollupStore {
    return MemoryPendingDwellRollupStore(mutableMapOf<String, PendingDwellRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDwellRollupStore(
    var inner: PendingDwellRollupStore,
    var cache: TtlCache<PendingDwellRollup>,
    var hits: Int,
    var misses: Int,
) : PendingDwellRollupStore {
    override fun find(code: String): PendingDwellRollup? {
        val hit = (cache.get(code) as PendingDwellRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDwellRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDwellRollup> = inner.list()
}

fun newCachedPendingDwellRollupStore(inner: PendingDwellRollupStore, ttl: Int, clock: Clock): PendingDwellRollupStore {
    return CachedPendingDwellRollupStore(
        inner = inner,
        cache = newTtlCache<PendingDwellRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dwell rollups. Empty fields match all. */
data class PendingDwellRollupQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDwellRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDwellRollup>): MutableList<PendingDwellRollup> {
        val matched = filter(items) { candidate: PendingDwellRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDwellRollups(store: PendingDwellRollupStore, query: PendingDwellRollupQuery): MutableList<PendingDwellRollup> {
    return query.apply(store.list())
}
