package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification records by code. */
interface QualificationRecordStore {
    fun find(code: String): QualificationRecord?
    fun save(item: QualificationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationRecordStore(
    var items: MutableMap<String, QualificationRecord>,
    var order: MutableList<String>,
) : QualificationRecordStore {
    override fun find(code: String): QualificationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationRecord) {
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

    override fun list(): MutableList<QualificationRecord> {
        val out = mutableListOf<QualificationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationRecordStore(): QualificationRecordStore {
    return MemoryQualificationRecordStore(mutableMapOf<String, QualificationRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationRecordStore(
    var inner: QualificationRecordStore,
    var cache: TtlCache<QualificationRecord>,
    var hits: Int,
    var misses: Int,
) : QualificationRecordStore {
    override fun find(code: String): QualificationRecord? {
        val hit = (cache.get(code) as QualificationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationRecord> = inner.list()
}

fun newCachedQualificationRecordStore(inner: QualificationRecordStore, ttl: Int, clock: Clock): QualificationRecordStore {
    return CachedQualificationRecordStore(
        inner = inner,
        cache = newTtlCache<QualificationRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification records. Empty fields match all. */
data class QualificationRecordQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationRecord): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationRecord>): MutableList<QualificationRecord> {
        val matched = filter(items) { candidate: QualificationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationRecords(store: QualificationRecordStore, query: QualificationRecordQuery): MutableList<QualificationRecord> {
    return query.apply(store.list())
}
