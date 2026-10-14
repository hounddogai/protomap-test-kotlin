package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict coaching entries by code. */
interface StrictCoachingEntryStore {
    fun find(code: String): StrictCoachingEntry?
    fun save(item: StrictCoachingEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCoachingEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCoachingEntryStore(
    var items: MutableMap<String, StrictCoachingEntry>,
    var order: MutableList<String>,
) : StrictCoachingEntryStore {
    override fun find(code: String): StrictCoachingEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCoachingEntry) {
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

    override fun list(): MutableList<StrictCoachingEntry> {
        val out = mutableListOf<StrictCoachingEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCoachingEntryStore(): StrictCoachingEntryStore {
    return MemoryStrictCoachingEntryStore(mutableMapOf<String, StrictCoachingEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCoachingEntryStore(
    var inner: StrictCoachingEntryStore,
    var cache: TtlCache<StrictCoachingEntry>,
    var hits: Int,
    var misses: Int,
) : StrictCoachingEntryStore {
    override fun find(code: String): StrictCoachingEntry? {
        val hit = (cache.get(code) as StrictCoachingEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCoachingEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCoachingEntry> = inner.list()
}

fun newCachedStrictCoachingEntryStore(inner: StrictCoachingEntryStore, ttl: Int, clock: Clock): StrictCoachingEntryStore {
    return CachedStrictCoachingEntryStore(
        inner = inner,
        cache = newTtlCache<StrictCoachingEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict coaching entries. Empty fields match all. */
data class StrictCoachingEntryQuery(
    var country: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCoachingEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCoachingEntry>): MutableList<StrictCoachingEntry> {
        val matched = filter(items) { candidate: StrictCoachingEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCoachingEntries(store: StrictCoachingEntryStore, query: StrictCoachingEntryQuery): MutableList<StrictCoachingEntry> {
    return query.apply(store.list())
}
