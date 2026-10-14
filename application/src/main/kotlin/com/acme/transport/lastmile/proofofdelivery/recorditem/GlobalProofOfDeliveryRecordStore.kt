package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global proof of delivery records by code. */
interface GlobalProofOfDeliveryRecordStore {
    fun find(code: String): GlobalProofOfDeliveryRecord?
    fun save(item: GlobalProofOfDeliveryRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalProofOfDeliveryRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalProofOfDeliveryRecordStore(
    var items: MutableMap<String, GlobalProofOfDeliveryRecord>,
    var order: MutableList<String>,
) : GlobalProofOfDeliveryRecordStore {
    override fun find(code: String): GlobalProofOfDeliveryRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalProofOfDeliveryRecord) {
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

    override fun list(): MutableList<GlobalProofOfDeliveryRecord> {
        val out = mutableListOf<GlobalProofOfDeliveryRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalProofOfDeliveryRecordStore(): GlobalProofOfDeliveryRecordStore {
    return MemoryGlobalProofOfDeliveryRecordStore(
        mutableMapOf<String, GlobalProofOfDeliveryRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalProofOfDeliveryRecordStore(
    var inner: GlobalProofOfDeliveryRecordStore,
    var cache: TtlCache<GlobalProofOfDeliveryRecord>,
    var hits: Int,
    var misses: Int,
) : GlobalProofOfDeliveryRecordStore {
    override fun find(code: String): GlobalProofOfDeliveryRecord? {
        val hit = (cache.get(code) as GlobalProofOfDeliveryRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalProofOfDeliveryRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalProofOfDeliveryRecord> = inner.list()
}

fun newCachedGlobalProofOfDeliveryRecordStore(inner: GlobalProofOfDeliveryRecordStore, ttl: Int, clock: Clock): GlobalProofOfDeliveryRecordStore {
    return CachedGlobalProofOfDeliveryRecordStore(
        inner = inner,
        cache = newTtlCache<GlobalProofOfDeliveryRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global proof of delivery records. Empty fields match all. */
data class GlobalProofOfDeliveryRecordQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalProofOfDeliveryRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalProofOfDeliveryRecord>): MutableList<GlobalProofOfDeliveryRecord> {
        val matched = filter(items) { candidate: GlobalProofOfDeliveryRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalProofOfDeliveryRecords(store: GlobalProofOfDeliveryRecordStore, query: GlobalProofOfDeliveryRecordQuery): MutableList<GlobalProofOfDeliveryRecord> {
    return query.apply(store.list())
}
