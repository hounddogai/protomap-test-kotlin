package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop entries by code. */
interface DropEntryStore {
    fun find(code: String): DropEntry?
    fun save(item: DropEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropEntryStore(
    var items: MutableMap<String, DropEntry>,
    var order: MutableList<String>,
) : DropEntryStore {
    override fun find(code: String): DropEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropEntry) {
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

    override fun list(): MutableList<DropEntry> {
        val out = mutableListOf<DropEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropEntryStore(): DropEntryStore {
    return MemoryDropEntryStore(mutableMapOf<String, DropEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropEntryStore(
    var inner: DropEntryStore,
    var cache: TtlCache<DropEntry>,
    var hits: Int,
    var misses: Int,
) : DropEntryStore {
    override fun find(code: String): DropEntry? {
        val hit = (cache.get(code) as DropEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropEntry> = inner.list()
}

fun newCachedDropEntryStore(inner: DropEntryStore, ttl: Int, clock: Clock): DropEntryStore {
    return CachedDropEntryStore(
        inner = inner,
        cache = newTtlCache<DropEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop entries. Empty fields match all. */
data class DropEntryQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropEntry): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<DropEntry>): MutableList<DropEntry> {
        val matched = filter(items) { candidate: DropEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropEntries(store: DropEntryStore, query: DropEntryQuery): MutableList<DropEntry> {
    return query.apply(store.list())
}
