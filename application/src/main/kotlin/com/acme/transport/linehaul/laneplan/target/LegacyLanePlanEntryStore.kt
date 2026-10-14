package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy lane plan entries by code. */
interface LegacyLanePlanEntryStore {
    fun find(code: String): LegacyLanePlanEntry?
    fun save(item: LegacyLanePlanEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLanePlanEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLanePlanEntryStore(
    var items: MutableMap<String, LegacyLanePlanEntry>,
    var order: MutableList<String>,
) : LegacyLanePlanEntryStore {
    override fun find(code: String): LegacyLanePlanEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLanePlanEntry) {
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

    override fun list(): MutableList<LegacyLanePlanEntry> {
        val out = mutableListOf<LegacyLanePlanEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLanePlanEntryStore(): LegacyLanePlanEntryStore {
    return MemoryLegacyLanePlanEntryStore(mutableMapOf<String, LegacyLanePlanEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLanePlanEntryStore(
    var inner: LegacyLanePlanEntryStore,
    var cache: TtlCache<LegacyLanePlanEntry>,
    var hits: Int,
    var misses: Int,
) : LegacyLanePlanEntryStore {
    override fun find(code: String): LegacyLanePlanEntry? {
        val hit = (cache.get(code) as LegacyLanePlanEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLanePlanEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLanePlanEntry> = inner.list()
}

fun newCachedLegacyLanePlanEntryStore(inner: LegacyLanePlanEntryStore, ttl: Int, clock: Clock): LegacyLanePlanEntryStore {
    return CachedLegacyLanePlanEntryStore(
        inner = inner,
        cache = newTtlCache<LegacyLanePlanEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy lane plan entries. Empty fields match all. */
data class LegacyLanePlanEntryQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLanePlanEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLanePlanEntry>): MutableList<LegacyLanePlanEntry> {
        val matched = filter(items) { candidate: LegacyLanePlanEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLanePlanEntries(store: LegacyLanePlanEntryStore, query: LegacyLanePlanEntryQuery): MutableList<LegacyLanePlanEntry> {
    return query.apply(store.list())
}
