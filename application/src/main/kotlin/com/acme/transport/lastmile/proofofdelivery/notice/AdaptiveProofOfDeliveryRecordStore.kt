package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive proof of delivery records by code. */
interface AdaptiveProofOfDeliveryRecordStore {
    fun find(code: String): AdaptiveProofOfDeliveryRecord?
    fun save(item: AdaptiveProofOfDeliveryRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveProofOfDeliveryRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveProofOfDeliveryRecordStore(
    var items: MutableMap<String, AdaptiveProofOfDeliveryRecord>,
    var order: MutableList<String>,
) : AdaptiveProofOfDeliveryRecordStore {
    override fun find(code: String): AdaptiveProofOfDeliveryRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveProofOfDeliveryRecord) {
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

    override fun list(): MutableList<AdaptiveProofOfDeliveryRecord> {
        val out = mutableListOf<AdaptiveProofOfDeliveryRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveProofOfDeliveryRecordStore(): AdaptiveProofOfDeliveryRecordStore {
    return MemoryAdaptiveProofOfDeliveryRecordStore(
        mutableMapOf<String, AdaptiveProofOfDeliveryRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveProofOfDeliveryRecordStore(
    var inner: AdaptiveProofOfDeliveryRecordStore,
    var cache: TtlCache<AdaptiveProofOfDeliveryRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveProofOfDeliveryRecordStore {
    override fun find(code: String): AdaptiveProofOfDeliveryRecord? {
        val hit = (cache.get(code) as AdaptiveProofOfDeliveryRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveProofOfDeliveryRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveProofOfDeliveryRecord> = inner.list()
}

fun newCachedAdaptiveProofOfDeliveryRecordStore(inner: AdaptiveProofOfDeliveryRecordStore, ttl: Int, clock: Clock): AdaptiveProofOfDeliveryRecordStore {
    return CachedAdaptiveProofOfDeliveryRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveProofOfDeliveryRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive proof of delivery records. Empty fields match all. */
data class AdaptiveProofOfDeliveryRecordQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveProofOfDeliveryRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveProofOfDeliveryRecord>): MutableList<AdaptiveProofOfDeliveryRecord> {
        val matched = filter(items) { candidate: AdaptiveProofOfDeliveryRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveProofOfDeliveryRecords(store: AdaptiveProofOfDeliveryRecordStore, query: AdaptiveProofOfDeliveryRecordQuery): MutableList<AdaptiveProofOfDeliveryRecord> {
    return query.apply(store.list())
}
