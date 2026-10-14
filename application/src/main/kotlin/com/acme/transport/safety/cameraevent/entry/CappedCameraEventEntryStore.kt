package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped camera event entries by code. */
interface CappedCameraEventEntryStore {
    fun find(code: String): CappedCameraEventEntry?
    fun save(item: CappedCameraEventEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCameraEventEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCameraEventEntryStore(
    var items: MutableMap<String, CappedCameraEventEntry>,
    var order: MutableList<String>,
) : CappedCameraEventEntryStore {
    override fun find(code: String): CappedCameraEventEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCameraEventEntry) {
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

    override fun list(): MutableList<CappedCameraEventEntry> {
        val out = mutableListOf<CappedCameraEventEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCameraEventEntryStore(): CappedCameraEventEntryStore {
    return MemoryCappedCameraEventEntryStore(
        mutableMapOf<String, CappedCameraEventEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCameraEventEntryStore(
    var inner: CappedCameraEventEntryStore,
    var cache: TtlCache<CappedCameraEventEntry>,
    var hits: Int,
    var misses: Int,
) : CappedCameraEventEntryStore {
    override fun find(code: String): CappedCameraEventEntry? {
        val hit = (cache.get(code) as CappedCameraEventEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCameraEventEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCameraEventEntry> = inner.list()
}

fun newCachedCappedCameraEventEntryStore(inner: CappedCameraEventEntryStore, ttl: Int, clock: Clock): CappedCameraEventEntryStore {
    return CachedCappedCameraEventEntryStore(
        inner = inner,
        cache = newTtlCache<CappedCameraEventEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped camera event entries. Empty fields match all. */
data class CappedCameraEventEntryQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCameraEventEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCameraEventEntry>): MutableList<CappedCameraEventEntry> {
        val matched = filter(items) { candidate: CappedCameraEventEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCameraEventEntries(store: CappedCameraEventEntryStore, query: CappedCameraEventEntryQuery): MutableList<CappedCameraEventEntry> {
    return query.apply(store.list())
}
