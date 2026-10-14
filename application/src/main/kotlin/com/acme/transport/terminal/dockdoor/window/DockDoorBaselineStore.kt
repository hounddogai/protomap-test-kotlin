package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door baselines by code. */
interface DockDoorBaselineStore {
    fun find(code: String): DockDoorBaseline?
    fun save(item: DockDoorBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorBaselineStore(
    var items: MutableMap<String, DockDoorBaseline>,
    var order: MutableList<String>,
) : DockDoorBaselineStore {
    override fun find(code: String): DockDoorBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorBaseline) {
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

    override fun list(): MutableList<DockDoorBaseline> {
        val out = mutableListOf<DockDoorBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorBaselineStore(): DockDoorBaselineStore {
    return MemoryDockDoorBaselineStore(mutableMapOf<String, DockDoorBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorBaselineStore(
    var inner: DockDoorBaselineStore,
    var cache: TtlCache<DockDoorBaseline>,
    var hits: Int,
    var misses: Int,
) : DockDoorBaselineStore {
    override fun find(code: String): DockDoorBaseline? {
        val hit = (cache.get(code) as DockDoorBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorBaseline> = inner.list()
}

fun newCachedDockDoorBaselineStore(inner: DockDoorBaselineStore, ttl: Int, clock: Clock): DockDoorBaselineStore {
    return CachedDockDoorBaselineStore(
        inner = inner,
        cache = newTtlCache<DockDoorBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door baselines. Empty fields match all. */
data class DockDoorBaselineQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorBaseline): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<DockDoorBaseline>): MutableList<DockDoorBaseline> {
        val matched = filter(items) { candidate: DockDoorBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorBaselines(store: DockDoorBaselineStore, query: DockDoorBaselineQuery): MutableList<DockDoorBaseline> {
    return query.apply(store.list())
}
