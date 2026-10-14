package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled camera event entries by code. */
interface ScheduledCameraEventEntryStore {
    fun find(code: String): ScheduledCameraEventEntry?
    fun save(item: ScheduledCameraEventEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCameraEventEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCameraEventEntryStore(
    var items: MutableMap<String, ScheduledCameraEventEntry>,
    var order: MutableList<String>,
) : ScheduledCameraEventEntryStore {
    override fun find(code: String): ScheduledCameraEventEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCameraEventEntry) {
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

    override fun list(): MutableList<ScheduledCameraEventEntry> {
        val out = mutableListOf<ScheduledCameraEventEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCameraEventEntryStore(): ScheduledCameraEventEntryStore {
    return MemoryScheduledCameraEventEntryStore(
        mutableMapOf<String, ScheduledCameraEventEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCameraEventEntryStore(
    var inner: ScheduledCameraEventEntryStore,
    var cache: TtlCache<ScheduledCameraEventEntry>,
    var hits: Int,
    var misses: Int,
) : ScheduledCameraEventEntryStore {
    override fun find(code: String): ScheduledCameraEventEntry? {
        val hit = (cache.get(code) as ScheduledCameraEventEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCameraEventEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCameraEventEntry> = inner.list()
}

fun newCachedScheduledCameraEventEntryStore(inner: ScheduledCameraEventEntryStore, ttl: Int, clock: Clock): ScheduledCameraEventEntryStore {
    return CachedScheduledCameraEventEntryStore(
        inner = inner,
        cache = newTtlCache<ScheduledCameraEventEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled camera event entries. Empty fields match all. */
data class ScheduledCameraEventEntryQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCameraEventEntry): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ScheduledCameraEventEntry>): MutableList<ScheduledCameraEventEntry> {
        val matched = filter(items) { candidate: ScheduledCameraEventEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCameraEventEntries(store: ScheduledCameraEventEntryStore, query: ScheduledCameraEventEntryQuery): MutableList<ScheduledCameraEventEntry> {
    return query.apply(store.list())
}
