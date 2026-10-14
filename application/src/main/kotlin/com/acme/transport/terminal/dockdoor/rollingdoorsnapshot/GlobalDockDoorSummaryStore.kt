package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global dock door summaries by code. */
interface GlobalDockDoorSummaryStore {
    fun find(code: String): GlobalDockDoorSummary?
    fun save(item: GlobalDockDoorSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDockDoorSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDockDoorSummaryStore(
    var items: MutableMap<String, GlobalDockDoorSummary>,
    var order: MutableList<String>,
) : GlobalDockDoorSummaryStore {
    override fun find(code: String): GlobalDockDoorSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDockDoorSummary) {
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

    override fun list(): MutableList<GlobalDockDoorSummary> {
        val out = mutableListOf<GlobalDockDoorSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDockDoorSummaryStore(): GlobalDockDoorSummaryStore {
    return MemoryGlobalDockDoorSummaryStore(
        mutableMapOf<String, GlobalDockDoorSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDockDoorSummaryStore(
    var inner: GlobalDockDoorSummaryStore,
    var cache: TtlCache<GlobalDockDoorSummary>,
    var hits: Int,
    var misses: Int,
) : GlobalDockDoorSummaryStore {
    override fun find(code: String): GlobalDockDoorSummary? {
        val hit = (cache.get(code) as GlobalDockDoorSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDockDoorSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDockDoorSummary> = inner.list()
}

fun newCachedGlobalDockDoorSummaryStore(inner: GlobalDockDoorSummaryStore, ttl: Int, clock: Clock): GlobalDockDoorSummaryStore {
    return CachedGlobalDockDoorSummaryStore(
        inner = inner,
        cache = newTtlCache<GlobalDockDoorSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global dock door summaries. Empty fields match all. */
data class GlobalDockDoorSummaryQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDockDoorSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDockDoorSummary>): MutableList<GlobalDockDoorSummary> {
        val matched = filter(items) { candidate: GlobalDockDoorSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDockDoorSummaries(store: GlobalDockDoorSummaryStore, query: GlobalDockDoorSummaryQuery): MutableList<GlobalDockDoorSummary> {
    return query.apply(store.list())
}
