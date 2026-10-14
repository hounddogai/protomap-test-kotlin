package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop records by code. */
interface DropRecordStore {
    fun find(code: String): DropRecord?
    fun save(item: DropRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropRecordStore(
    var items: MutableMap<String, DropRecord>,
    var order: MutableList<String>,
) : DropRecordStore {
    override fun find(code: String): DropRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropRecord) {
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

    override fun list(): MutableList<DropRecord> {
        val out = mutableListOf<DropRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropRecordStore(): DropRecordStore {
    return MemoryDropRecordStore(mutableMapOf<String, DropRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropRecordStore(
    var inner: DropRecordStore,
    var cache: TtlCache<DropRecord>,
    var hits: Int,
    var misses: Int,
) : DropRecordStore {
    override fun find(code: String): DropRecord? {
        val hit = (cache.get(code) as DropRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropRecord> = inner.list()
}

fun newCachedDropRecordStore(inner: DropRecordStore, ttl: Int, clock: Clock): DropRecordStore {
    return CachedDropRecordStore(
        inner = inner,
        cache = newTtlCache<DropRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop records. Empty fields match all. */
data class DropRecordQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropRecord): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropRecord>): MutableList<DropRecord> {
        val matched = filter(items) { candidate: DropRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropRecords(store: DropRecordStore, query: DropRecordQuery): MutableList<DropRecord> {
    return query.apply(store.list())
}
