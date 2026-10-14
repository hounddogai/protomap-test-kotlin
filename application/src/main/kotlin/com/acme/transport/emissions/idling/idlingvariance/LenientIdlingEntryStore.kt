package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient idling entries by code. */
interface LenientIdlingEntryStore {
    fun find(code: String): LenientIdlingEntry?
    fun save(item: LenientIdlingEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIdlingEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIdlingEntryStore(
    var items: MutableMap<String, LenientIdlingEntry>,
    var order: MutableList<String>,
) : LenientIdlingEntryStore {
    override fun find(code: String): LenientIdlingEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIdlingEntry) {
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

    override fun list(): MutableList<LenientIdlingEntry> {
        val out = mutableListOf<LenientIdlingEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIdlingEntryStore(): LenientIdlingEntryStore {
    return MemoryLenientIdlingEntryStore(mutableMapOf<String, LenientIdlingEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIdlingEntryStore(
    var inner: LenientIdlingEntryStore,
    var cache: TtlCache<LenientIdlingEntry>,
    var hits: Int,
    var misses: Int,
) : LenientIdlingEntryStore {
    override fun find(code: String): LenientIdlingEntry? {
        val hit = (cache.get(code) as LenientIdlingEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIdlingEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIdlingEntry> = inner.list()
}

fun newCachedLenientIdlingEntryStore(inner: LenientIdlingEntryStore, ttl: Int, clock: Clock): LenientIdlingEntryStore {
    return CachedLenientIdlingEntryStore(
        inner = inner,
        cache = newTtlCache<LenientIdlingEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient idling entries. Empty fields match all. */
data class LenientIdlingEntryQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIdlingEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientIdlingEntry>): MutableList<LenientIdlingEntry> {
        val matched = filter(items) { candidate: LenientIdlingEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIdlingEntries(store: LenientIdlingEntryStore, query: LenientIdlingEntryQuery): MutableList<LenientIdlingEntry> {
    return query.apply(store.list())
}
