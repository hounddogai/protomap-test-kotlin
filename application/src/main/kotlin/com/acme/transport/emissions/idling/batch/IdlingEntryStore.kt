package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling entries by code. */
interface IdlingEntryStore {
    fun find(code: String): IdlingEntry?
    fun save(item: IdlingEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingEntryStore(
    var items: MutableMap<String, IdlingEntry>,
    var order: MutableList<String>,
) : IdlingEntryStore {
    override fun find(code: String): IdlingEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingEntry) {
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

    override fun list(): MutableList<IdlingEntry> {
        val out = mutableListOf<IdlingEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingEntryStore(): IdlingEntryStore {
    return MemoryIdlingEntryStore(mutableMapOf<String, IdlingEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingEntryStore(
    var inner: IdlingEntryStore,
    var cache: TtlCache<IdlingEntry>,
    var hits: Int,
    var misses: Int,
) : IdlingEntryStore {
    override fun find(code: String): IdlingEntry? {
        val hit = (cache.get(code) as IdlingEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingEntry> = inner.list()
}

fun newCachedIdlingEntryStore(inner: IdlingEntryStore, ttl: Int, clock: Clock): IdlingEntryStore {
    return CachedIdlingEntryStore(
        inner = inner,
        cache = newTtlCache<IdlingEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling entries. Empty fields match all. */
data class IdlingEntryQuery(
    var category: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingEntry>): MutableList<IdlingEntry> {
        val matched = filter(items) { candidate: IdlingEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingEntries(store: IdlingEntryStore, query: IdlingEntryQuery): MutableList<IdlingEntry> {
    return query.apply(store.list())
}
