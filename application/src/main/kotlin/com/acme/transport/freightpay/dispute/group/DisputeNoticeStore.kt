package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute notices by code. */
interface DisputeNoticeStore {
    fun find(code: String): DisputeNotice?
    fun save(item: DisputeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeNoticeStore(
    var items: MutableMap<String, DisputeNotice>,
    var order: MutableList<String>,
) : DisputeNoticeStore {
    override fun find(code: String): DisputeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeNotice) {
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

    override fun list(): MutableList<DisputeNotice> {
        val out = mutableListOf<DisputeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeNoticeStore(): DisputeNoticeStore {
    return MemoryDisputeNoticeStore(mutableMapOf<String, DisputeNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeNoticeStore(
    var inner: DisputeNoticeStore,
    var cache: TtlCache<DisputeNotice>,
    var hits: Int,
    var misses: Int,
) : DisputeNoticeStore {
    override fun find(code: String): DisputeNotice? {
        val hit = (cache.get(code) as DisputeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeNotice> = inner.list()
}

fun newCachedDisputeNoticeStore(inner: DisputeNoticeStore, ttl: Int, clock: Clock): DisputeNoticeStore {
    return CachedDisputeNoticeStore(
        inner = inner,
        cache = newTtlCache<DisputeNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute notices. Empty fields match all. */
data class DisputeNoticeQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeNotice>): MutableList<DisputeNotice> {
        val matched = filter(items) { candidate: DisputeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeNotices(store: DisputeNoticeStore, query: DisputeNoticeQuery): MutableList<DisputeNotice> {
    return query.apply(store.list())
}
