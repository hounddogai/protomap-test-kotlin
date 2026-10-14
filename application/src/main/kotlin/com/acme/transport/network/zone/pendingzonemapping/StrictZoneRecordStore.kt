package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict zone records by code. */
interface StrictZoneRecordStore {
    fun find(code: String): StrictZoneRecord?
    fun save(item: StrictZoneRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictZoneRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictZoneRecordStore(
    var items: MutableMap<String, StrictZoneRecord>,
    var order: MutableList<String>,
) : StrictZoneRecordStore {
    override fun find(code: String): StrictZoneRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictZoneRecord) {
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

    override fun list(): MutableList<StrictZoneRecord> {
        val out = mutableListOf<StrictZoneRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictZoneRecordStore(): StrictZoneRecordStore {
    return MemoryStrictZoneRecordStore(mutableMapOf<String, StrictZoneRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictZoneRecordStore(
    var inner: StrictZoneRecordStore,
    var cache: TtlCache<StrictZoneRecord>,
    var hits: Int,
    var misses: Int,
) : StrictZoneRecordStore {
    override fun find(code: String): StrictZoneRecord? {
        val hit = (cache.get(code) as StrictZoneRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictZoneRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictZoneRecord> = inner.list()
}

fun newCachedStrictZoneRecordStore(inner: StrictZoneRecordStore, ttl: Int, clock: Clock): StrictZoneRecordStore {
    return CachedStrictZoneRecordStore(
        inner = inner,
        cache = newTtlCache<StrictZoneRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict zone records. Empty fields match all. */
data class StrictZoneRecordQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictZoneRecord): Boolean {
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

    fun apply(items: MutableList<StrictZoneRecord>): MutableList<StrictZoneRecord> {
        val matched = filter(items) { candidate: StrictZoneRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictZoneRecords(store: StrictZoneRecordStore, query: StrictZoneRecordQuery): MutableList<StrictZoneRecord> {
    return query.apply(store.list())
}
