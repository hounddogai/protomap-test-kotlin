package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling dwell cycles by code. */
interface RollingDwellCycleStore {
    fun find(code: String): RollingDwellCycle?
    fun save(item: RollingDwellCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDwellCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDwellCycleStore(
    var items: MutableMap<String, RollingDwellCycle>,
    var order: MutableList<String>,
) : RollingDwellCycleStore {
    override fun find(code: String): RollingDwellCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDwellCycle) {
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

    override fun list(): MutableList<RollingDwellCycle> {
        val out = mutableListOf<RollingDwellCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDwellCycleStore(): RollingDwellCycleStore {
    return MemoryRollingDwellCycleStore(mutableMapOf<String, RollingDwellCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDwellCycleStore(
    var inner: RollingDwellCycleStore,
    var cache: TtlCache<RollingDwellCycle>,
    var hits: Int,
    var misses: Int,
) : RollingDwellCycleStore {
    override fun find(code: String): RollingDwellCycle? {
        val hit = (cache.get(code) as RollingDwellCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDwellCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDwellCycle> = inner.list()
}

fun newCachedRollingDwellCycleStore(inner: RollingDwellCycleStore, ttl: Int, clock: Clock): RollingDwellCycleStore {
    return CachedRollingDwellCycleStore(
        inner = inner,
        cache = newTtlCache<RollingDwellCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dwell cycles. Empty fields match all. */
data class RollingDwellCycleQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDwellCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDwellCycle>): MutableList<RollingDwellCycle> {
        val matched = filter(items) { candidate: RollingDwellCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDwellCycles(store: RollingDwellCycleStore, query: RollingDwellCycleQuery): MutableList<RollingDwellCycle> {
    return query.apply(store.list())
}
