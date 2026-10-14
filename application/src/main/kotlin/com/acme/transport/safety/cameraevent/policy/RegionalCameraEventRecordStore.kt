package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional camera event records by code. */
interface RegionalCameraEventRecordStore {
    fun find(code: String): RegionalCameraEventRecord?
    fun save(item: RegionalCameraEventRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCameraEventRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCameraEventRecordStore(
    var items: MutableMap<String, RegionalCameraEventRecord>,
    var order: MutableList<String>,
) : RegionalCameraEventRecordStore {
    override fun find(code: String): RegionalCameraEventRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCameraEventRecord) {
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

    override fun list(): MutableList<RegionalCameraEventRecord> {
        val out = mutableListOf<RegionalCameraEventRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCameraEventRecordStore(): RegionalCameraEventRecordStore {
    return MemoryRegionalCameraEventRecordStore(
        mutableMapOf<String, RegionalCameraEventRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCameraEventRecordStore(
    var inner: RegionalCameraEventRecordStore,
    var cache: TtlCache<RegionalCameraEventRecord>,
    var hits: Int,
    var misses: Int,
) : RegionalCameraEventRecordStore {
    override fun find(code: String): RegionalCameraEventRecord? {
        val hit = (cache.get(code) as RegionalCameraEventRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCameraEventRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCameraEventRecord> = inner.list()
}

fun newCachedRegionalCameraEventRecordStore(inner: RegionalCameraEventRecordStore, ttl: Int, clock: Clock): RegionalCameraEventRecordStore {
    return CachedRegionalCameraEventRecordStore(
        inner = inner,
        cache = newTtlCache<RegionalCameraEventRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional camera event records. Empty fields match all. */
data class RegionalCameraEventRecordQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCameraEventRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCameraEventRecord>): MutableList<RegionalCameraEventRecord> {
        val matched = filter(items) { candidate: RegionalCameraEventRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCameraEventRecords(store: RegionalCameraEventRecordStore, query: RegionalCameraEventRecordQuery): MutableList<RegionalCameraEventRecord> {
    return query.apply(store.list())
}
