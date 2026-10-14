package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional parcel entries by code. */
interface RegionalParcelEntryStore {
    fun find(code: String): RegionalParcelEntry?
    fun save(item: RegionalParcelEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalParcelEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalParcelEntryStore(
    var items: MutableMap<String, RegionalParcelEntry>,
    var order: MutableList<String>,
) : RegionalParcelEntryStore {
    override fun find(code: String): RegionalParcelEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalParcelEntry) {
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

    override fun list(): MutableList<RegionalParcelEntry> {
        val out = mutableListOf<RegionalParcelEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalParcelEntryStore(): RegionalParcelEntryStore {
    return MemoryRegionalParcelEntryStore(mutableMapOf<String, RegionalParcelEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalParcelEntryStore(
    var inner: RegionalParcelEntryStore,
    var cache: TtlCache<RegionalParcelEntry>,
    var hits: Int,
    var misses: Int,
) : RegionalParcelEntryStore {
    override fun find(code: String): RegionalParcelEntry? {
        val hit = (cache.get(code) as RegionalParcelEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalParcelEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalParcelEntry> = inner.list()
}

fun newCachedRegionalParcelEntryStore(inner: RegionalParcelEntryStore, ttl: Int, clock: Clock): RegionalParcelEntryStore {
    return CachedRegionalParcelEntryStore(
        inner = inner,
        cache = newTtlCache<RegionalParcelEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional parcel entries. Empty fields match all. */
data class RegionalParcelEntryQuery(
    var locale: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalParcelEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalParcelEntry>): MutableList<RegionalParcelEntry> {
        val matched = filter(items) { candidate: RegionalParcelEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalParcelEntries(store: RegionalParcelEntryStore, query: RegionalParcelEntryQuery): MutableList<RegionalParcelEntry> {
    return query.apply(store.list())
}
