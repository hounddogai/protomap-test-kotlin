package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident entries by code. */
interface IncidentEntryStore {
    fun find(code: String): IncidentEntry?
    fun save(item: IncidentEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentEntryStore(
    var items: MutableMap<String, IncidentEntry>,
    var order: MutableList<String>,
) : IncidentEntryStore {
    override fun find(code: String): IncidentEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentEntry) {
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

    override fun list(): MutableList<IncidentEntry> {
        val out = mutableListOf<IncidentEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentEntryStore(): IncidentEntryStore {
    return MemoryIncidentEntryStore(mutableMapOf<String, IncidentEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentEntryStore(
    var inner: IncidentEntryStore,
    var cache: TtlCache<IncidentEntry>,
    var hits: Int,
    var misses: Int,
) : IncidentEntryStore {
    override fun find(code: String): IncidentEntry? {
        val hit = (cache.get(code) as IncidentEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentEntry> = inner.list()
}

fun newCachedIncidentEntryStore(inner: IncidentEntryStore, ttl: Int, clock: Clock): IncidentEntryStore {
    return CachedIncidentEntryStore(
        inner = inner,
        cache = newTtlCache<IncidentEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident entries. Empty fields match all. */
data class IncidentEntryQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<IncidentEntry>): MutableList<IncidentEntry> {
        val matched = filter(items) { candidate: IncidentEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentEntries(store: IncidentEntryStore, query: IncidentEntryQuery): MutableList<IncidentEntry> {
    return query.apply(store.list())
}
