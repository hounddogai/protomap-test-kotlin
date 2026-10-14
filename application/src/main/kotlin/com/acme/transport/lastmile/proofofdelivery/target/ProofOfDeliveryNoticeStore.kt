package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery notices by code. */
interface ProofOfDeliveryNoticeStore {
    fun find(code: String): ProofOfDeliveryNotice?
    fun save(item: ProofOfDeliveryNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryNoticeStore(
    var items: MutableMap<String, ProofOfDeliveryNotice>,
    var order: MutableList<String>,
) : ProofOfDeliveryNoticeStore {
    override fun find(code: String): ProofOfDeliveryNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryNotice) {
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

    override fun list(): MutableList<ProofOfDeliveryNotice> {
        val out = mutableListOf<ProofOfDeliveryNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryNoticeStore(): ProofOfDeliveryNoticeStore {
    return MemoryProofOfDeliveryNoticeStore(
        mutableMapOf<String, ProofOfDeliveryNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryNoticeStore(
    var inner: ProofOfDeliveryNoticeStore,
    var cache: TtlCache<ProofOfDeliveryNotice>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryNoticeStore {
    override fun find(code: String): ProofOfDeliveryNotice? {
        val hit = (cache.get(code) as ProofOfDeliveryNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryNotice> = inner.list()
}

fun newCachedProofOfDeliveryNoticeStore(inner: ProofOfDeliveryNoticeStore, ttl: Int, clock: Clock): ProofOfDeliveryNoticeStore {
    return CachedProofOfDeliveryNoticeStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery notices. Empty fields match all. */
data class ProofOfDeliveryNoticeQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryNotice>): MutableList<ProofOfDeliveryNotice> {
        val matched = filter(items) { candidate: ProofOfDeliveryNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryNotices(store: ProofOfDeliveryNoticeStore, query: ProofOfDeliveryNoticeQuery): MutableList<ProofOfDeliveryNotice> {
    return query.apply(store.list())
}
