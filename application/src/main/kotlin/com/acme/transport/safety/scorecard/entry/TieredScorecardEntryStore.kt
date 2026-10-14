package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered scorecard entries by code. */
interface TieredScorecardEntryStore {
    fun find(code: String): TieredScorecardEntry?
    fun save(item: TieredScorecardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredScorecardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredScorecardEntryStore(
    var items: MutableMap<String, TieredScorecardEntry>,
    var order: MutableList<String>,
) : TieredScorecardEntryStore {
    override fun find(code: String): TieredScorecardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredScorecardEntry) {
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

    override fun list(): MutableList<TieredScorecardEntry> {
        val out = mutableListOf<TieredScorecardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredScorecardEntryStore(): TieredScorecardEntryStore {
    return MemoryTieredScorecardEntryStore(
        mutableMapOf<String, TieredScorecardEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredScorecardEntryStore(
    var inner: TieredScorecardEntryStore,
    var cache: TtlCache<TieredScorecardEntry>,
    var hits: Int,
    var misses: Int,
) : TieredScorecardEntryStore {
    override fun find(code: String): TieredScorecardEntry? {
        val hit = (cache.get(code) as TieredScorecardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredScorecardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredScorecardEntry> = inner.list()
}

fun newCachedTieredScorecardEntryStore(inner: TieredScorecardEntryStore, ttl: Int, clock: Clock): TieredScorecardEntryStore {
    return CachedTieredScorecardEntryStore(
        inner = inner,
        cache = newTtlCache<TieredScorecardEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered scorecard entries. Empty fields match all. */
data class TieredScorecardEntryQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredScorecardEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredScorecardEntry>): MutableList<TieredScorecardEntry> {
        val matched = filter(items) { candidate: TieredScorecardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredScorecardEntries(store: TieredScorecardEntryStore, query: TieredScorecardEntryQuery): MutableList<TieredScorecardEntry> {
    return query.apply(store.list())
}
