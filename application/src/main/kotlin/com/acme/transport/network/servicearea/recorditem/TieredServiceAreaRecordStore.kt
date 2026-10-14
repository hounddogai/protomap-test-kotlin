package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered service area records by code. */
interface TieredServiceAreaRecordStore {
    fun find(code: String): TieredServiceAreaRecord?
    fun save(item: TieredServiceAreaRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredServiceAreaRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredServiceAreaRecordStore(
    var items: MutableMap<String, TieredServiceAreaRecord>,
    var order: MutableList<String>,
) : TieredServiceAreaRecordStore {
    override fun find(code: String): TieredServiceAreaRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredServiceAreaRecord) {
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

    override fun list(): MutableList<TieredServiceAreaRecord> {
        val out = mutableListOf<TieredServiceAreaRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredServiceAreaRecordStore(): TieredServiceAreaRecordStore {
    return MemoryTieredServiceAreaRecordStore(
        mutableMapOf<String, TieredServiceAreaRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredServiceAreaRecordStore(
    var inner: TieredServiceAreaRecordStore,
    var cache: TtlCache<TieredServiceAreaRecord>,
    var hits: Int,
    var misses: Int,
) : TieredServiceAreaRecordStore {
    override fun find(code: String): TieredServiceAreaRecord? {
        val hit = (cache.get(code) as TieredServiceAreaRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredServiceAreaRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredServiceAreaRecord> = inner.list()
}

fun newCachedTieredServiceAreaRecordStore(inner: TieredServiceAreaRecordStore, ttl: Int, clock: Clock): TieredServiceAreaRecordStore {
    return CachedTieredServiceAreaRecordStore(
        inner = inner,
        cache = newTtlCache<TieredServiceAreaRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered service area records. Empty fields match all. */
data class TieredServiceAreaRecordQuery(
    var unit: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredServiceAreaRecord): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredServiceAreaRecord>): MutableList<TieredServiceAreaRecord> {
        val matched = filter(items) { candidate: TieredServiceAreaRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredServiceAreaRecords(store: TieredServiceAreaRecordStore, query: TieredServiceAreaRecordQuery): MutableList<TieredServiceAreaRecord> {
    return query.apply(store.list())
}
