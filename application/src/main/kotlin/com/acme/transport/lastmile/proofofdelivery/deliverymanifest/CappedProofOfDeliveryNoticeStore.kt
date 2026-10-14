package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped proof of delivery notices by code. */
interface CappedProofOfDeliveryNoticeStore {
    fun find(code: String): CappedProofOfDeliveryNotice?
    fun save(item: CappedProofOfDeliveryNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedProofOfDeliveryNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedProofOfDeliveryNoticeStore(
    var items: MutableMap<String, CappedProofOfDeliveryNotice>,
    var order: MutableList<String>,
) : CappedProofOfDeliveryNoticeStore {
    override fun find(code: String): CappedProofOfDeliveryNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedProofOfDeliveryNotice) {
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

    override fun list(): MutableList<CappedProofOfDeliveryNotice> {
        val out = mutableListOf<CappedProofOfDeliveryNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedProofOfDeliveryNoticeStore(): CappedProofOfDeliveryNoticeStore {
    return MemoryCappedProofOfDeliveryNoticeStore(
        mutableMapOf<String, CappedProofOfDeliveryNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedProofOfDeliveryNoticeStore(
    var inner: CappedProofOfDeliveryNoticeStore,
    var cache: TtlCache<CappedProofOfDeliveryNotice>,
    var hits: Int,
    var misses: Int,
) : CappedProofOfDeliveryNoticeStore {
    override fun find(code: String): CappedProofOfDeliveryNotice? {
        val hit = (cache.get(code) as CappedProofOfDeliveryNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedProofOfDeliveryNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedProofOfDeliveryNotice> = inner.list()
}

fun newCachedCappedProofOfDeliveryNoticeStore(inner: CappedProofOfDeliveryNoticeStore, ttl: Int, clock: Clock): CappedProofOfDeliveryNoticeStore {
    return CachedCappedProofOfDeliveryNoticeStore(
        inner = inner,
        cache = newTtlCache<CappedProofOfDeliveryNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped proof of delivery notices. Empty fields match all. */
data class CappedProofOfDeliveryNoticeQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedProofOfDeliveryNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedProofOfDeliveryNotice>): MutableList<CappedProofOfDeliveryNotice> {
        val matched = filter(items) { candidate: CappedProofOfDeliveryNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedProofOfDeliveryNotices(store: CappedProofOfDeliveryNoticeStore, query: CappedProofOfDeliveryNoticeQuery): MutableList<CappedProofOfDeliveryNotice> {
    return query.apply(store.list())
}
