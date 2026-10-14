package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub entries by code. */
interface HubEntryStore {
    fun find(code: String): HubEntry?
    fun save(item: HubEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubEntryStore(
    var items: MutableMap<String, HubEntry>,
    var order: MutableList<String>,
) : HubEntryStore {
    override fun find(code: String): HubEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubEntry) {
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

    override fun list(): MutableList<HubEntry> {
        val out = mutableListOf<HubEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubEntryStore(): HubEntryStore {
    return MemoryHubEntryStore(mutableMapOf<String, HubEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubEntryStore(
    var inner: HubEntryStore,
    var cache: TtlCache<HubEntry>,
    var hits: Int,
    var misses: Int,
) : HubEntryStore {
    override fun find(code: String): HubEntry? {
        val hit = (cache.get(code) as HubEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubEntry> = inner.list()
}

fun newCachedHubEntryStore(inner: HubEntryStore, ttl: Int, clock: Clock): HubEntryStore {
    return CachedHubEntryStore(
        inner = inner,
        cache = newTtlCache<HubEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub entries. Empty fields match all. */
data class HubEntryQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubEntry>): MutableList<HubEntry> {
        val matched = filter(items) { candidate: HubEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubEntries(store: HubEntryStore, query: HubEntryQuery): MutableList<HubEntry> = query.apply(store.list())
