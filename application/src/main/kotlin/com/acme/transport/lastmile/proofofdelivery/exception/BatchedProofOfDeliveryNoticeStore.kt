package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched proof of delivery notices by code. */
interface BatchedProofOfDeliveryNoticeStore {
    fun find(code: String): BatchedProofOfDeliveryNotice?
    fun save(item: BatchedProofOfDeliveryNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedProofOfDeliveryNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedProofOfDeliveryNoticeStore(
    var items: MutableMap<String, BatchedProofOfDeliveryNotice>,
    var order: MutableList<String>,
) : BatchedProofOfDeliveryNoticeStore {
    override fun find(code: String): BatchedProofOfDeliveryNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedProofOfDeliveryNotice) {
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

    override fun list(): MutableList<BatchedProofOfDeliveryNotice> {
        val out = mutableListOf<BatchedProofOfDeliveryNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedProofOfDeliveryNoticeStore(): BatchedProofOfDeliveryNoticeStore {
    return MemoryBatchedProofOfDeliveryNoticeStore(
        mutableMapOf<String, BatchedProofOfDeliveryNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedProofOfDeliveryNoticeStore(
    var inner: BatchedProofOfDeliveryNoticeStore,
    var cache: TtlCache<BatchedProofOfDeliveryNotice>,
    var hits: Int,
    var misses: Int,
) : BatchedProofOfDeliveryNoticeStore {
    override fun find(code: String): BatchedProofOfDeliveryNotice? {
        val hit = (cache.get(code) as BatchedProofOfDeliveryNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedProofOfDeliveryNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedProofOfDeliveryNotice> = inner.list()
}

fun newCachedBatchedProofOfDeliveryNoticeStore(inner: BatchedProofOfDeliveryNoticeStore, ttl: Int, clock: Clock): BatchedProofOfDeliveryNoticeStore {
    return CachedBatchedProofOfDeliveryNoticeStore(
        inner = inner,
        cache = newTtlCache<BatchedProofOfDeliveryNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched proof of delivery notices. Empty fields match all. */
data class BatchedProofOfDeliveryNoticeQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedProofOfDeliveryNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<BatchedProofOfDeliveryNotice>): MutableList<BatchedProofOfDeliveryNotice> {
        val matched = filter(items) { candidate: BatchedProofOfDeliveryNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedProofOfDeliveryNotices(store: BatchedProofOfDeliveryNoticeStore, query: BatchedProofOfDeliveryNoticeQuery): MutableList<BatchedProofOfDeliveryNotice> {
    return query.apply(store.list())
}
