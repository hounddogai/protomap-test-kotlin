package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event entries by code. */
interface CameraEventEntryStore {
    fun find(code: String): CameraEventEntry?
    fun save(item: CameraEventEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventEntryStore(
    var items: MutableMap<String, CameraEventEntry>,
    var order: MutableList<String>,
) : CameraEventEntryStore {
    override fun find(code: String): CameraEventEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventEntry) {
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

    override fun list(): MutableList<CameraEventEntry> {
        val out = mutableListOf<CameraEventEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventEntryStore(): CameraEventEntryStore {
    return MemoryCameraEventEntryStore(mutableMapOf<String, CameraEventEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventEntryStore(
    var inner: CameraEventEntryStore,
    var cache: TtlCache<CameraEventEntry>,
    var hits: Int,
    var misses: Int,
) : CameraEventEntryStore {
    override fun find(code: String): CameraEventEntry? {
        val hit = (cache.get(code) as CameraEventEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventEntry> = inner.list()
}

fun newCachedCameraEventEntryStore(inner: CameraEventEntryStore, ttl: Int, clock: Clock): CameraEventEntryStore {
    return CachedCameraEventEntryStore(
        inner = inner,
        cache = newTtlCache<CameraEventEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event entries. Empty fields match all. */
data class CameraEventEntryQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventEntry>): MutableList<CameraEventEntry> {
        val matched = filter(items) { candidate: CameraEventEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventEntries(store: CameraEventEntryStore, query: CameraEventEntryQuery): MutableList<CameraEventEntry> {
    return query.apply(store.list())
}
