package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy incident entries by code. */
interface LegacyIncidentEntryStore {
    fun find(code: String): LegacyIncidentEntry?
    fun save(item: LegacyIncidentEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyIncidentEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyIncidentEntryStore(
    var items: MutableMap<String, LegacyIncidentEntry>,
    var order: MutableList<String>,
) : LegacyIncidentEntryStore {
    override fun find(code: String): LegacyIncidentEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyIncidentEntry) {
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

    override fun list(): MutableList<LegacyIncidentEntry> {
        val out = mutableListOf<LegacyIncidentEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyIncidentEntryStore(): LegacyIncidentEntryStore {
    return MemoryLegacyIncidentEntryStore(mutableMapOf<String, LegacyIncidentEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyIncidentEntryStore(
    var inner: LegacyIncidentEntryStore,
    var cache: TtlCache<LegacyIncidentEntry>,
    var hits: Int,
    var misses: Int,
) : LegacyIncidentEntryStore {
    override fun find(code: String): LegacyIncidentEntry? {
        val hit = (cache.get(code) as LegacyIncidentEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyIncidentEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyIncidentEntry> = inner.list()
}

fun newCachedLegacyIncidentEntryStore(inner: LegacyIncidentEntryStore, ttl: Int, clock: Clock): LegacyIncidentEntryStore {
    return CachedLegacyIncidentEntryStore(
        inner = inner,
        cache = newTtlCache<LegacyIncidentEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy incident entries. Empty fields match all. */
data class LegacyIncidentEntryQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyIncidentEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LegacyIncidentEntry>): MutableList<LegacyIncidentEntry> {
        val matched = filter(items) { candidate: LegacyIncidentEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyIncidentEntries(store: LegacyIncidentEntryStore, query: LegacyIncidentEntryQuery): MutableList<LegacyIncidentEntry> {
    return query.apply(store.list())
}
