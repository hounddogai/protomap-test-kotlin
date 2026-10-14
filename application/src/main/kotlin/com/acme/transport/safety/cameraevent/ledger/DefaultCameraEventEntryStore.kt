package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default camera event entries by code. */
interface DefaultCameraEventEntryStore {
    fun find(code: String): DefaultCameraEventEntry?
    fun save(item: DefaultCameraEventEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCameraEventEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCameraEventEntryStore(
    var items: MutableMap<String, DefaultCameraEventEntry>,
    var order: MutableList<String>,
) : DefaultCameraEventEntryStore {
    override fun find(code: String): DefaultCameraEventEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCameraEventEntry) {
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

    override fun list(): MutableList<DefaultCameraEventEntry> {
        val out = mutableListOf<DefaultCameraEventEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCameraEventEntryStore(): DefaultCameraEventEntryStore {
    return MemoryDefaultCameraEventEntryStore(
        mutableMapOf<String, DefaultCameraEventEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCameraEventEntryStore(
    var inner: DefaultCameraEventEntryStore,
    var cache: TtlCache<DefaultCameraEventEntry>,
    var hits: Int,
    var misses: Int,
) : DefaultCameraEventEntryStore {
    override fun find(code: String): DefaultCameraEventEntry? {
        val hit = (cache.get(code) as DefaultCameraEventEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCameraEventEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCameraEventEntry> = inner.list()
}

fun newCachedDefaultCameraEventEntryStore(inner: DefaultCameraEventEntryStore, ttl: Int, clock: Clock): DefaultCameraEventEntryStore {
    return CachedDefaultCameraEventEntryStore(
        inner = inner,
        cache = newTtlCache<DefaultCameraEventEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default camera event entries. Empty fields match all. */
data class DefaultCameraEventEntryQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCameraEventEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCameraEventEntry>): MutableList<DefaultCameraEventEntry> {
        val matched = filter(items) { candidate: DefaultCameraEventEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCameraEventEntries(store: DefaultCameraEventEntryStore, query: DefaultCameraEventEntryQuery): MutableList<DefaultCameraEventEntry> {
    return query.apply(store.list())
}
