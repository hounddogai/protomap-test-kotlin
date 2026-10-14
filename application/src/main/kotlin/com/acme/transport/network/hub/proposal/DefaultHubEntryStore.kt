package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default hub entries by code. */
interface DefaultHubEntryStore {
    fun find(code: String): DefaultHubEntry?
    fun save(item: DefaultHubEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultHubEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultHubEntryStore(
    var items: MutableMap<String, DefaultHubEntry>,
    var order: MutableList<String>,
) : DefaultHubEntryStore {
    override fun find(code: String): DefaultHubEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultHubEntry) {
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

    override fun list(): MutableList<DefaultHubEntry> {
        val out = mutableListOf<DefaultHubEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultHubEntryStore(): DefaultHubEntryStore {
    return MemoryDefaultHubEntryStore(mutableMapOf<String, DefaultHubEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultHubEntryStore(
    var inner: DefaultHubEntryStore,
    var cache: TtlCache<DefaultHubEntry>,
    var hits: Int,
    var misses: Int,
) : DefaultHubEntryStore {
    override fun find(code: String): DefaultHubEntry? {
        val hit = (cache.get(code) as DefaultHubEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultHubEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultHubEntry> = inner.list()
}

fun newCachedDefaultHubEntryStore(inner: DefaultHubEntryStore, ttl: Int, clock: Clock): DefaultHubEntryStore {
    return CachedDefaultHubEntryStore(
        inner = inner,
        cache = newTtlCache<DefaultHubEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default hub entries. Empty fields match all. */
data class DefaultHubEntryQuery(
    var channel: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultHubEntry): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<DefaultHubEntry>): MutableList<DefaultHubEntry> {
        val matched = filter(items) { candidate: DefaultHubEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultHubEntries(store: DefaultHubEntryStore, query: DefaultHubEntryQuery): MutableList<DefaultHubEntry> {
    return query.apply(store.list())
}
