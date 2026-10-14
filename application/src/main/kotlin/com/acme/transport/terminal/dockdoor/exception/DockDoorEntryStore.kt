package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door entries by code. */
interface DockDoorEntryStore {
    fun find(code: String): DockDoorEntry?
    fun save(item: DockDoorEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorEntryStore(
    var items: MutableMap<String, DockDoorEntry>,
    var order: MutableList<String>,
) : DockDoorEntryStore {
    override fun find(code: String): DockDoorEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorEntry) {
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

    override fun list(): MutableList<DockDoorEntry> {
        val out = mutableListOf<DockDoorEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorEntryStore(): DockDoorEntryStore {
    return MemoryDockDoorEntryStore(mutableMapOf<String, DockDoorEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorEntryStore(
    var inner: DockDoorEntryStore,
    var cache: TtlCache<DockDoorEntry>,
    var hits: Int,
    var misses: Int,
) : DockDoorEntryStore {
    override fun find(code: String): DockDoorEntry? {
        val hit = (cache.get(code) as DockDoorEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorEntry> = inner.list()
}

fun newCachedDockDoorEntryStore(inner: DockDoorEntryStore, ttl: Int, clock: Clock): DockDoorEntryStore {
    return CachedDockDoorEntryStore(
        inner = inner,
        cache = newTtlCache<DockDoorEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door entries. Empty fields match all. */
data class DockDoorEntryQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorEntry>): MutableList<DockDoorEntry> {
        val matched = filter(items) { candidate: DockDoorEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorEntries(store: DockDoorEntryStore, query: DockDoorEntryQuery): MutableList<DockDoorEntry> {
    return query.apply(store.list())
}
