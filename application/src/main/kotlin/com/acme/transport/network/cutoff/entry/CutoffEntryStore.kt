package com.acme.transport.network.cutoff.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff entries by code. */
interface CutoffEntryStore {
    fun find(code: String): CutoffEntry?
    fun save(item: CutoffEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffEntryStore(
    var items: MutableMap<String, CutoffEntry>,
    var order: MutableList<String>,
) : CutoffEntryStore {
    override fun find(code: String): CutoffEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffEntry) {
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

    override fun list(): MutableList<CutoffEntry> {
        val out = mutableListOf<CutoffEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffEntryStore(): CutoffEntryStore {
    return MemoryCutoffEntryStore(mutableMapOf<String, CutoffEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffEntryStore(
    var inner: CutoffEntryStore,
    var cache: TtlCache<CutoffEntry>,
    var hits: Int,
    var misses: Int,
) : CutoffEntryStore {
    override fun find(code: String): CutoffEntry? {
        val hit = (cache.get(code) as CutoffEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffEntry> = inner.list()
}

fun newCachedCutoffEntryStore(inner: CutoffEntryStore, ttl: Int, clock: Clock): CutoffEntryStore {
    return CachedCutoffEntryStore(
        inner = inner,
        cache = newTtlCache<CutoffEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff entries. Empty fields match all. */
data class CutoffEntryQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffEntry>): MutableList<CutoffEntry> {
        val matched = filter(items) { candidate: CutoffEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffEntries(store: CutoffEntryStore, query: CutoffEntryQuery): MutableList<CutoffEntry> {
    return query.apply(store.list())
}
