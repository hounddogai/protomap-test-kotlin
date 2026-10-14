package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy fuel burn entries by code. */
interface LegacyFuelBurnEntryStore {
    fun find(code: String): LegacyFuelBurnEntry?
    fun save(item: LegacyFuelBurnEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyFuelBurnEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyFuelBurnEntryStore(
    var items: MutableMap<String, LegacyFuelBurnEntry>,
    var order: MutableList<String>,
) : LegacyFuelBurnEntryStore {
    override fun find(code: String): LegacyFuelBurnEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyFuelBurnEntry) {
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

    override fun list(): MutableList<LegacyFuelBurnEntry> {
        val out = mutableListOf<LegacyFuelBurnEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyFuelBurnEntryStore(): LegacyFuelBurnEntryStore {
    return MemoryLegacyFuelBurnEntryStore(mutableMapOf<String, LegacyFuelBurnEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyFuelBurnEntryStore(
    var inner: LegacyFuelBurnEntryStore,
    var cache: TtlCache<LegacyFuelBurnEntry>,
    var hits: Int,
    var misses: Int,
) : LegacyFuelBurnEntryStore {
    override fun find(code: String): LegacyFuelBurnEntry? {
        val hit = (cache.get(code) as LegacyFuelBurnEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyFuelBurnEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyFuelBurnEntry> = inner.list()
}

fun newCachedLegacyFuelBurnEntryStore(inner: LegacyFuelBurnEntryStore, ttl: Int, clock: Clock): LegacyFuelBurnEntryStore {
    return CachedLegacyFuelBurnEntryStore(
        inner = inner,
        cache = newTtlCache<LegacyFuelBurnEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy fuel burn entries. Empty fields match all. */
data class LegacyFuelBurnEntryQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyFuelBurnEntry): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyFuelBurnEntry>): MutableList<LegacyFuelBurnEntry> {
        val matched = filter(items) { candidate: LegacyFuelBurnEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyFuelBurnEntries(store: LegacyFuelBurnEntryStore, query: LegacyFuelBurnEntryQuery): MutableList<LegacyFuelBurnEntry> {
    return query.apply(store.list())
}
