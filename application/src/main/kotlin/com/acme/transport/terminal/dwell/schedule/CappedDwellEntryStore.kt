package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dwell entries by code. */
interface CappedDwellEntryStore {
    fun find(code: String): CappedDwellEntry?
    fun save(item: CappedDwellEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDwellEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDwellEntryStore(
    var items: MutableMap<String, CappedDwellEntry>,
    var order: MutableList<String>,
) : CappedDwellEntryStore {
    override fun find(code: String): CappedDwellEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDwellEntry) {
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

    override fun list(): MutableList<CappedDwellEntry> {
        val out = mutableListOf<CappedDwellEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDwellEntryStore(): CappedDwellEntryStore {
    return MemoryCappedDwellEntryStore(mutableMapOf<String, CappedDwellEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDwellEntryStore(
    var inner: CappedDwellEntryStore,
    var cache: TtlCache<CappedDwellEntry>,
    var hits: Int,
    var misses: Int,
) : CappedDwellEntryStore {
    override fun find(code: String): CappedDwellEntry? {
        val hit = (cache.get(code) as CappedDwellEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDwellEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDwellEntry> = inner.list()
}

fun newCachedCappedDwellEntryStore(inner: CappedDwellEntryStore, ttl: Int, clock: Clock): CappedDwellEntryStore {
    return CachedCappedDwellEntryStore(
        inner = inner,
        cache = newTtlCache<CappedDwellEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dwell entries. Empty fields match all. */
data class CappedDwellEntryQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDwellEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDwellEntry>): MutableList<CappedDwellEntry> {
        val matched = filter(items) { candidate: CappedDwellEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDwellEntries(store: CappedDwellEntryStore, query: CappedDwellEntryQuery): MutableList<CappedDwellEntry> {
    return query.apply(store.list())
}
