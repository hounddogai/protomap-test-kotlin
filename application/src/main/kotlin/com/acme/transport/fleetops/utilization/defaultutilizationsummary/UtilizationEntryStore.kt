package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization entries by code. */
interface UtilizationEntryStore {
    fun find(code: String): UtilizationEntry?
    fun save(item: UtilizationEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationEntryStore(
    var items: MutableMap<String, UtilizationEntry>,
    var order: MutableList<String>,
) : UtilizationEntryStore {
    override fun find(code: String): UtilizationEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationEntry) {
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

    override fun list(): MutableList<UtilizationEntry> {
        val out = mutableListOf<UtilizationEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationEntryStore(): UtilizationEntryStore {
    return MemoryUtilizationEntryStore(mutableMapOf<String, UtilizationEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationEntryStore(
    var inner: UtilizationEntryStore,
    var cache: TtlCache<UtilizationEntry>,
    var hits: Int,
    var misses: Int,
) : UtilizationEntryStore {
    override fun find(code: String): UtilizationEntry? {
        val hit = (cache.get(code) as UtilizationEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationEntry> = inner.list()
}

fun newCachedUtilizationEntryStore(inner: UtilizationEntryStore, ttl: Int, clock: Clock): UtilizationEntryStore {
    return CachedUtilizationEntryStore(
        inner = inner,
        cache = newTtlCache<UtilizationEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization entries. Empty fields match all. */
data class UtilizationEntryQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationEntry>): MutableList<UtilizationEntry> {
        val matched = filter(items) { candidate: UtilizationEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationEntries(store: UtilizationEntryStore, query: UtilizationEntryQuery): MutableList<UtilizationEntry> {
    return query.apply(store.list())
}
