package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff entries by code. */
interface TariffEntryStore {
    fun find(code: String): TariffEntry?
    fun save(item: TariffEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffEntryStore(
    var items: MutableMap<String, TariffEntry>,
    var order: MutableList<String>,
) : TariffEntryStore {
    override fun find(code: String): TariffEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffEntry) {
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

    override fun list(): MutableList<TariffEntry> {
        val out = mutableListOf<TariffEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffEntryStore(): TariffEntryStore {
    return MemoryTariffEntryStore(mutableMapOf<String, TariffEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffEntryStore(
    var inner: TariffEntryStore,
    var cache: TtlCache<TariffEntry>,
    var hits: Int,
    var misses: Int,
) : TariffEntryStore {
    override fun find(code: String): TariffEntry? {
        val hit = (cache.get(code) as TariffEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffEntry> = inner.list()
}

fun newCachedTariffEntryStore(inner: TariffEntryStore, ttl: Int, clock: Clock): TariffEntryStore {
    return CachedTariffEntryStore(
        inner = inner,
        cache = newTtlCache<TariffEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff entries. Empty fields match all. */
data class TariffEntryQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffEntry>): MutableList<TariffEntry> {
        val matched = filter(items) { candidate: TariffEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffEntries(store: TariffEntryStore, query: TariffEntryQuery): MutableList<TariffEntry> {
    return query.apply(store.list())
}
