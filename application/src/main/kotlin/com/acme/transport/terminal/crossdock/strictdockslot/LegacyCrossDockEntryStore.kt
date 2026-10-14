package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy cross dock entries by code. */
interface LegacyCrossDockEntryStore {
    fun find(code: String): LegacyCrossDockEntry?
    fun save(item: LegacyCrossDockEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCrossDockEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCrossDockEntryStore(
    var items: MutableMap<String, LegacyCrossDockEntry>,
    var order: MutableList<String>,
) : LegacyCrossDockEntryStore {
    override fun find(code: String): LegacyCrossDockEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCrossDockEntry) {
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

    override fun list(): MutableList<LegacyCrossDockEntry> {
        val out = mutableListOf<LegacyCrossDockEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCrossDockEntryStore(): LegacyCrossDockEntryStore {
    return MemoryLegacyCrossDockEntryStore(
        mutableMapOf<String, LegacyCrossDockEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCrossDockEntryStore(
    var inner: LegacyCrossDockEntryStore,
    var cache: TtlCache<LegacyCrossDockEntry>,
    var hits: Int,
    var misses: Int,
) : LegacyCrossDockEntryStore {
    override fun find(code: String): LegacyCrossDockEntry? {
        val hit = (cache.get(code) as LegacyCrossDockEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCrossDockEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCrossDockEntry> = inner.list()
}

fun newCachedLegacyCrossDockEntryStore(inner: LegacyCrossDockEntryStore, ttl: Int, clock: Clock): LegacyCrossDockEntryStore {
    return CachedLegacyCrossDockEntryStore(
        inner = inner,
        cache = newTtlCache<LegacyCrossDockEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cross dock entries. Empty fields match all. */
data class LegacyCrossDockEntryQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCrossDockEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCrossDockEntry>): MutableList<LegacyCrossDockEntry> {
        val matched = filter(items) { candidate: LegacyCrossDockEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCrossDockEntries(store: LegacyCrossDockEntryStore, query: LegacyCrossDockEntryQuery): MutableList<LegacyCrossDockEntry> {
    return query.apply(store.list())
}
