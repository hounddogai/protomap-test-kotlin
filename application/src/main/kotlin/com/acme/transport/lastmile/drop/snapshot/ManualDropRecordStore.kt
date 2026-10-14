package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual drop records by code. */
interface ManualDropRecordStore {
    fun find(code: String): ManualDropRecord?
    fun save(item: ManualDropRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDropRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDropRecordStore(
    var items: MutableMap<String, ManualDropRecord>,
    var order: MutableList<String>,
) : ManualDropRecordStore {
    override fun find(code: String): ManualDropRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDropRecord) {
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

    override fun list(): MutableList<ManualDropRecord> {
        val out = mutableListOf<ManualDropRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDropRecordStore(): ManualDropRecordStore {
    return MemoryManualDropRecordStore(mutableMapOf<String, ManualDropRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDropRecordStore(
    var inner: ManualDropRecordStore,
    var cache: TtlCache<ManualDropRecord>,
    var hits: Int,
    var misses: Int,
) : ManualDropRecordStore {
    override fun find(code: String): ManualDropRecord? {
        val hit = (cache.get(code) as ManualDropRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDropRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDropRecord> = inner.list()
}

fun newCachedManualDropRecordStore(inner: ManualDropRecordStore, ttl: Int, clock: Clock): ManualDropRecordStore {
    return CachedManualDropRecordStore(
        inner = inner,
        cache = newTtlCache<ManualDropRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual drop records. Empty fields match all. */
data class ManualDropRecordQuery(
    var region: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDropRecord): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDropRecord>): MutableList<ManualDropRecord> {
        val matched = filter(items) { candidate: ManualDropRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDropRecords(store: ManualDropRecordStore, query: ManualDropRecordQuery): MutableList<ManualDropRecord> {
    return query.apply(store.list())
}
