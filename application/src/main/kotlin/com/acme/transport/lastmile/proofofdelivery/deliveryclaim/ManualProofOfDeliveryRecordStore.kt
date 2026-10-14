package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual proof of delivery records by code. */
interface ManualProofOfDeliveryRecordStore {
    fun find(code: String): ManualProofOfDeliveryRecord?
    fun save(item: ManualProofOfDeliveryRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualProofOfDeliveryRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualProofOfDeliveryRecordStore(
    var items: MutableMap<String, ManualProofOfDeliveryRecord>,
    var order: MutableList<String>,
) : ManualProofOfDeliveryRecordStore {
    override fun find(code: String): ManualProofOfDeliveryRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualProofOfDeliveryRecord) {
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

    override fun list(): MutableList<ManualProofOfDeliveryRecord> {
        val out = mutableListOf<ManualProofOfDeliveryRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualProofOfDeliveryRecordStore(): ManualProofOfDeliveryRecordStore {
    return MemoryManualProofOfDeliveryRecordStore(
        mutableMapOf<String, ManualProofOfDeliveryRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualProofOfDeliveryRecordStore(
    var inner: ManualProofOfDeliveryRecordStore,
    var cache: TtlCache<ManualProofOfDeliveryRecord>,
    var hits: Int,
    var misses: Int,
) : ManualProofOfDeliveryRecordStore {
    override fun find(code: String): ManualProofOfDeliveryRecord? {
        val hit = (cache.get(code) as ManualProofOfDeliveryRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualProofOfDeliveryRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualProofOfDeliveryRecord> = inner.list()
}

fun newCachedManualProofOfDeliveryRecordStore(inner: ManualProofOfDeliveryRecordStore, ttl: Int, clock: Clock): ManualProofOfDeliveryRecordStore {
    return CachedManualProofOfDeliveryRecordStore(
        inner = inner,
        cache = newTtlCache<ManualProofOfDeliveryRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual proof of delivery records. Empty fields match all. */
data class ManualProofOfDeliveryRecordQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualProofOfDeliveryRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualProofOfDeliveryRecord>): MutableList<ManualProofOfDeliveryRecord> {
        val matched = filter(items) { candidate: ManualProofOfDeliveryRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualProofOfDeliveryRecords(store: ManualProofOfDeliveryRecordStore, query: ManualProofOfDeliveryRecordQuery): MutableList<ManualProofOfDeliveryRecord> {
    return query.apply(store.list())
}
