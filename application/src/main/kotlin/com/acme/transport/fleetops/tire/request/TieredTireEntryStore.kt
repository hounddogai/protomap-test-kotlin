package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered tire entries by code. */
interface TieredTireEntryStore {
    fun find(code: String): TieredTireEntry?
    fun save(item: TieredTireEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTireEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTireEntryStore(
    var items: MutableMap<String, TieredTireEntry>,
    var order: MutableList<String>,
) : TieredTireEntryStore {
    override fun find(code: String): TieredTireEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTireEntry) {
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

    override fun list(): MutableList<TieredTireEntry> {
        val out = mutableListOf<TieredTireEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTireEntryStore(): TieredTireEntryStore {
    return MemoryTieredTireEntryStore(mutableMapOf<String, TieredTireEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTireEntryStore(
    var inner: TieredTireEntryStore,
    var cache: TtlCache<TieredTireEntry>,
    var hits: Int,
    var misses: Int,
) : TieredTireEntryStore {
    override fun find(code: String): TieredTireEntry? {
        val hit = (cache.get(code) as TieredTireEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTireEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTireEntry> = inner.list()
}

fun newCachedTieredTireEntryStore(inner: TieredTireEntryStore, ttl: Int, clock: Clock): TieredTireEntryStore {
    return CachedTieredTireEntryStore(
        inner = inner,
        cache = newTtlCache<TieredTireEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tire entries. Empty fields match all. */
data class TieredTireEntryQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTireEntry): Boolean {
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

    fun apply(items: MutableList<TieredTireEntry>): MutableList<TieredTireEntry> {
        val matched = filter(items) { candidate: TieredTireEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTireEntries(store: TieredTireEntryStore, query: TieredTireEntryQuery): MutableList<TieredTireEntry> {
    return query.apply(store.list())
}
