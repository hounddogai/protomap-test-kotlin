package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional redelivery entries by code. */
interface RegionalRedeliveryEntryStore {
    fun find(code: String): RegionalRedeliveryEntry?
    fun save(item: RegionalRedeliveryEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRedeliveryEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRedeliveryEntryStore(
    var items: MutableMap<String, RegionalRedeliveryEntry>,
    var order: MutableList<String>,
) : RegionalRedeliveryEntryStore {
    override fun find(code: String): RegionalRedeliveryEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRedeliveryEntry) {
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

    override fun list(): MutableList<RegionalRedeliveryEntry> {
        val out = mutableListOf<RegionalRedeliveryEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRedeliveryEntryStore(): RegionalRedeliveryEntryStore {
    return MemoryRegionalRedeliveryEntryStore(
        mutableMapOf<String, RegionalRedeliveryEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRedeliveryEntryStore(
    var inner: RegionalRedeliveryEntryStore,
    var cache: TtlCache<RegionalRedeliveryEntry>,
    var hits: Int,
    var misses: Int,
) : RegionalRedeliveryEntryStore {
    override fun find(code: String): RegionalRedeliveryEntry? {
        val hit = (cache.get(code) as RegionalRedeliveryEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRedeliveryEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRedeliveryEntry> = inner.list()
}

fun newCachedRegionalRedeliveryEntryStore(inner: RegionalRedeliveryEntryStore, ttl: Int, clock: Clock): RegionalRedeliveryEntryStore {
    return CachedRegionalRedeliveryEntryStore(
        inner = inner,
        cache = newTtlCache<RegionalRedeliveryEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional redelivery entries. Empty fields match all. */
data class RegionalRedeliveryEntryQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRedeliveryEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RegionalRedeliveryEntry>): MutableList<RegionalRedeliveryEntry> {
        val matched = filter(items) { candidate: RegionalRedeliveryEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRedeliveryEntries(store: RegionalRedeliveryEntryStore, query: RegionalRedeliveryEntryQuery): MutableList<RegionalRedeliveryEntry> {
    return query.apply(store.list())
}
