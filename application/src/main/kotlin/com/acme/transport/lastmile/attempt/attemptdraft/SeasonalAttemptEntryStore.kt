package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal attempt entries by code. */
interface SeasonalAttemptEntryStore {
    fun find(code: String): SeasonalAttemptEntry?
    fun save(item: SeasonalAttemptEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAttemptEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAttemptEntryStore(
    var items: MutableMap<String, SeasonalAttemptEntry>,
    var order: MutableList<String>,
) : SeasonalAttemptEntryStore {
    override fun find(code: String): SeasonalAttemptEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAttemptEntry) {
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

    override fun list(): MutableList<SeasonalAttemptEntry> {
        val out = mutableListOf<SeasonalAttemptEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAttemptEntryStore(): SeasonalAttemptEntryStore {
    return MemorySeasonalAttemptEntryStore(
        mutableMapOf<String, SeasonalAttemptEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAttemptEntryStore(
    var inner: SeasonalAttemptEntryStore,
    var cache: TtlCache<SeasonalAttemptEntry>,
    var hits: Int,
    var misses: Int,
) : SeasonalAttemptEntryStore {
    override fun find(code: String): SeasonalAttemptEntry? {
        val hit = (cache.get(code) as SeasonalAttemptEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAttemptEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAttemptEntry> = inner.list()
}

fun newCachedSeasonalAttemptEntryStore(inner: SeasonalAttemptEntryStore, ttl: Int, clock: Clock): SeasonalAttemptEntryStore {
    return CachedSeasonalAttemptEntryStore(
        inner = inner,
        cache = newTtlCache<SeasonalAttemptEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal attempt entries. Empty fields match all. */
data class SeasonalAttemptEntryQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAttemptEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAttemptEntry>): MutableList<SeasonalAttemptEntry> {
        val matched = filter(items) { candidate: SeasonalAttemptEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAttemptEntries(store: SeasonalAttemptEntryStore, query: SeasonalAttemptEntryQuery): MutableList<SeasonalAttemptEntry> {
    return query.apply(store.list())
}
