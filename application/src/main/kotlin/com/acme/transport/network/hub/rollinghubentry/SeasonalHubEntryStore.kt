package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal hub entries by code. */
interface SeasonalHubEntryStore {
    fun find(code: String): SeasonalHubEntry?
    fun save(item: SeasonalHubEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalHubEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalHubEntryStore(
    var items: MutableMap<String, SeasonalHubEntry>,
    var order: MutableList<String>,
) : SeasonalHubEntryStore {
    override fun find(code: String): SeasonalHubEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalHubEntry) {
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

    override fun list(): MutableList<SeasonalHubEntry> {
        val out = mutableListOf<SeasonalHubEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalHubEntryStore(): SeasonalHubEntryStore {
    return MemorySeasonalHubEntryStore(mutableMapOf<String, SeasonalHubEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalHubEntryStore(
    var inner: SeasonalHubEntryStore,
    var cache: TtlCache<SeasonalHubEntry>,
    var hits: Int,
    var misses: Int,
) : SeasonalHubEntryStore {
    override fun find(code: String): SeasonalHubEntry? {
        val hit = (cache.get(code) as SeasonalHubEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalHubEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalHubEntry> = inner.list()
}

fun newCachedSeasonalHubEntryStore(inner: SeasonalHubEntryStore, ttl: Int, clock: Clock): SeasonalHubEntryStore {
    return CachedSeasonalHubEntryStore(
        inner = inner,
        cache = newTtlCache<SeasonalHubEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal hub entries. Empty fields match all. */
data class SeasonalHubEntryQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalHubEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalHubEntry>): MutableList<SeasonalHubEntry> {
        val matched = filter(items) { candidate: SeasonalHubEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalHubEntries(store: SeasonalHubEntryStore, query: SeasonalHubEntryQuery): MutableList<SeasonalHubEntry> {
    return query.apply(store.list())
}
