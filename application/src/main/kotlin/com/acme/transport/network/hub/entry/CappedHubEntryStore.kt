package com.acme.transport.network.hub.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped hub entries by code. */
interface CappedHubEntryStore {
    fun find(code: String): CappedHubEntry?
    fun save(item: CappedHubEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedHubEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedHubEntryStore(
    var items: MutableMap<String, CappedHubEntry>,
    var order: MutableList<String>,
) : CappedHubEntryStore {
    override fun find(code: String): CappedHubEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedHubEntry) {
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

    override fun list(): MutableList<CappedHubEntry> {
        val out = mutableListOf<CappedHubEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedHubEntryStore(): CappedHubEntryStore {
    return MemoryCappedHubEntryStore(mutableMapOf<String, CappedHubEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedHubEntryStore(
    var inner: CappedHubEntryStore,
    var cache: TtlCache<CappedHubEntry>,
    var hits: Int,
    var misses: Int,
) : CappedHubEntryStore {
    override fun find(code: String): CappedHubEntry? {
        val hit = (cache.get(code) as CappedHubEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedHubEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedHubEntry> = inner.list()
}

fun newCachedCappedHubEntryStore(inner: CappedHubEntryStore, ttl: Int, clock: Clock): CappedHubEntryStore {
    return CachedCappedHubEntryStore(
        inner = inner,
        cache = newTtlCache<CappedHubEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped hub entries. Empty fields match all. */
data class CappedHubEntryQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedHubEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedHubEntry>): MutableList<CappedHubEntry> {
        val matched = filter(items) { candidate: CappedHubEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedHubEntries(store: CappedHubEntryStore, query: CappedHubEntryQuery): MutableList<CappedHubEntry> {
    return query.apply(store.list())
}
