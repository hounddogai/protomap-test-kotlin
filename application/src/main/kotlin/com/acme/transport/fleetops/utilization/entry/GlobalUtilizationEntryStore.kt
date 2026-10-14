package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global utilization entries by code. */
interface GlobalUtilizationEntryStore {
    fun find(code: String): GlobalUtilizationEntry?
    fun save(item: GlobalUtilizationEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalUtilizationEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalUtilizationEntryStore(
    var items: MutableMap<String, GlobalUtilizationEntry>,
    var order: MutableList<String>,
) : GlobalUtilizationEntryStore {
    override fun find(code: String): GlobalUtilizationEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalUtilizationEntry) {
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

    override fun list(): MutableList<GlobalUtilizationEntry> {
        val out = mutableListOf<GlobalUtilizationEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalUtilizationEntryStore(): GlobalUtilizationEntryStore {
    return MemoryGlobalUtilizationEntryStore(
        mutableMapOf<String, GlobalUtilizationEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalUtilizationEntryStore(
    var inner: GlobalUtilizationEntryStore,
    var cache: TtlCache<GlobalUtilizationEntry>,
    var hits: Int,
    var misses: Int,
) : GlobalUtilizationEntryStore {
    override fun find(code: String): GlobalUtilizationEntry? {
        val hit = (cache.get(code) as GlobalUtilizationEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalUtilizationEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalUtilizationEntry> = inner.list()
}

fun newCachedGlobalUtilizationEntryStore(inner: GlobalUtilizationEntryStore, ttl: Int, clock: Clock): GlobalUtilizationEntryStore {
    return CachedGlobalUtilizationEntryStore(
        inner = inner,
        cache = newTtlCache<GlobalUtilizationEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global utilization entries. Empty fields match all. */
data class GlobalUtilizationEntryQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalUtilizationEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalUtilizationEntry>): MutableList<GlobalUtilizationEntry> {
        val matched = filter(items) { candidate: GlobalUtilizationEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalUtilizationEntries(store: GlobalUtilizationEntryStore, query: GlobalUtilizationEntryQuery): MutableList<GlobalUtilizationEntry> {
    return query.apply(store.list())
}
