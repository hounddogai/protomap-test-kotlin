package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay records by code. */
interface RelayRecordStore {
    fun find(code: String): RelayRecord?
    fun save(item: RelayRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayRecordStore(
    var items: MutableMap<String, RelayRecord>,
    var order: MutableList<String>,
) : RelayRecordStore {
    override fun find(code: String): RelayRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayRecord) {
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

    override fun list(): MutableList<RelayRecord> {
        val out = mutableListOf<RelayRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayRecordStore(): RelayRecordStore {
    return MemoryRelayRecordStore(mutableMapOf<String, RelayRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayRecordStore(
    var inner: RelayRecordStore,
    var cache: TtlCache<RelayRecord>,
    var hits: Int,
    var misses: Int,
) : RelayRecordStore {
    override fun find(code: String): RelayRecord? {
        val hit = (cache.get(code) as RelayRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayRecord> = inner.list()
}

fun newCachedRelayRecordStore(inner: RelayRecordStore, ttl: Int, clock: Clock): RelayRecordStore {
    return CachedRelayRecordStore(
        inner = inner,
        cache = newTtlCache<RelayRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay records. Empty fields match all. */
data class RelayRecordQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayRecord>): MutableList<RelayRecord> {
        val matched = filter(items) { candidate: RelayRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayRecords(store: RelayRecordStore, query: RelayRecordQuery): MutableList<RelayRecord> {
    return query.apply(store.list())
}
