package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard entries by code. */
interface ScorecardEntryStore {
    fun find(code: String): ScorecardEntry?
    fun save(item: ScorecardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardEntryStore(
    var items: MutableMap<String, ScorecardEntry>,
    var order: MutableList<String>,
) : ScorecardEntryStore {
    override fun find(code: String): ScorecardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardEntry) {
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

    override fun list(): MutableList<ScorecardEntry> {
        val out = mutableListOf<ScorecardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardEntryStore(): ScorecardEntryStore {
    return MemoryScorecardEntryStore(mutableMapOf<String, ScorecardEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardEntryStore(
    var inner: ScorecardEntryStore,
    var cache: TtlCache<ScorecardEntry>,
    var hits: Int,
    var misses: Int,
) : ScorecardEntryStore {
    override fun find(code: String): ScorecardEntry? {
        val hit = (cache.get(code) as ScorecardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardEntry> = inner.list()
}

fun newCachedScorecardEntryStore(inner: ScorecardEntryStore, ttl: Int, clock: Clock): ScorecardEntryStore {
    return CachedScorecardEntryStore(
        inner = inner,
        cache = newTtlCache<ScorecardEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard entries. Empty fields match all. */
data class ScorecardEntryQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardEntry): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<ScorecardEntry>): MutableList<ScorecardEntry> {
        val matched = filter(items) { candidate: ScorecardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardEntries(store: ScorecardEntryStore, query: ScorecardEntryQuery): MutableList<ScorecardEntry> {
    return query.apply(store.list())
}
