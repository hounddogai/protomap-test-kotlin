package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default idling entries by code. */
interface DefaultIdlingEntryStore {
    fun find(code: String): DefaultIdlingEntry?
    fun save(item: DefaultIdlingEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultIdlingEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultIdlingEntryStore(
    var items: MutableMap<String, DefaultIdlingEntry>,
    var order: MutableList<String>,
) : DefaultIdlingEntryStore {
    override fun find(code: String): DefaultIdlingEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultIdlingEntry) {
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

    override fun list(): MutableList<DefaultIdlingEntry> {
        val out = mutableListOf<DefaultIdlingEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultIdlingEntryStore(): DefaultIdlingEntryStore {
    return MemoryDefaultIdlingEntryStore(mutableMapOf<String, DefaultIdlingEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultIdlingEntryStore(
    var inner: DefaultIdlingEntryStore,
    var cache: TtlCache<DefaultIdlingEntry>,
    var hits: Int,
    var misses: Int,
) : DefaultIdlingEntryStore {
    override fun find(code: String): DefaultIdlingEntry? {
        val hit = (cache.get(code) as DefaultIdlingEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultIdlingEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultIdlingEntry> = inner.list()
}

fun newCachedDefaultIdlingEntryStore(inner: DefaultIdlingEntryStore, ttl: Int, clock: Clock): DefaultIdlingEntryStore {
    return CachedDefaultIdlingEntryStore(
        inner = inner,
        cache = newTtlCache<DefaultIdlingEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default idling entries. Empty fields match all. */
data class DefaultIdlingEntryQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultIdlingEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<DefaultIdlingEntry>): MutableList<DefaultIdlingEntry> {
        val matched = filter(items) { candidate: DefaultIdlingEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultIdlingEntries(store: DefaultIdlingEntryStore, query: DefaultIdlingEntryQuery): MutableList<DefaultIdlingEntry> {
    return query.apply(store.list())
}
