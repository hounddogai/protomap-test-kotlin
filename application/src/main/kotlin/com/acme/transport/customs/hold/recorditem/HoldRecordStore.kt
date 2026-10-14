package com.acme.transport.customs.hold.recorditem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold records by code. */
interface HoldRecordStore {
    fun find(code: String): HoldRecord?
    fun save(item: HoldRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldRecordStore(
    var items: MutableMap<String, HoldRecord>,
    var order: MutableList<String>,
) : HoldRecordStore {
    override fun find(code: String): HoldRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldRecord) {
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

    override fun list(): MutableList<HoldRecord> {
        val out = mutableListOf<HoldRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldRecordStore(): HoldRecordStore {
    return MemoryHoldRecordStore(mutableMapOf<String, HoldRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldRecordStore(
    var inner: HoldRecordStore,
    var cache: TtlCache<HoldRecord>,
    var hits: Int,
    var misses: Int,
) : HoldRecordStore {
    override fun find(code: String): HoldRecord? {
        val hit = (cache.get(code) as HoldRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldRecord> = inner.list()
}

fun newCachedHoldRecordStore(inner: HoldRecordStore, ttl: Int, clock: Clock): HoldRecordStore {
    return CachedHoldRecordStore(
        inner = inner,
        cache = newTtlCache<HoldRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold records. Empty fields match all. */
data class HoldRecordQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldRecord>): MutableList<HoldRecord> {
        val matched = filter(items) { candidate: HoldRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldRecords(store: HoldRecordStore, query: HoldRecordQuery): MutableList<HoldRecord> {
    return query.apply(store.list())
}
