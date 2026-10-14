package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell entries by code. */
interface DwellEntryStore {
    fun find(code: String): DwellEntry?
    fun save(item: DwellEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellEntryStore(
    var items: MutableMap<String, DwellEntry>,
    var order: MutableList<String>,
) : DwellEntryStore {
    override fun find(code: String): DwellEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellEntry) {
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

    override fun list(): MutableList<DwellEntry> {
        val out = mutableListOf<DwellEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellEntryStore(): DwellEntryStore {
    return MemoryDwellEntryStore(mutableMapOf<String, DwellEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellEntryStore(
    var inner: DwellEntryStore,
    var cache: TtlCache<DwellEntry>,
    var hits: Int,
    var misses: Int,
) : DwellEntryStore {
    override fun find(code: String): DwellEntry? {
        val hit = (cache.get(code) as DwellEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellEntry> = inner.list()
}

fun newCachedDwellEntryStore(inner: DwellEntryStore, ttl: Int, clock: Clock): DwellEntryStore {
    return CachedDwellEntryStore(
        inner = inner,
        cache = newTtlCache<DwellEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell entries. Empty fields match all. */
data class DwellEntryQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellEntry): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellEntry>): MutableList<DwellEntry> {
        val matched = filter(items) { candidate: DwellEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellEntries(store: DwellEntryStore, query: DwellEntryQuery): MutableList<DwellEntry> {
    return query.apply(store.list())
}
