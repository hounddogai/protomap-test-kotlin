package com.acme.transport.customs.broker.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker notices by code. */
interface BrokerNoticeStore {
    fun find(code: String): BrokerNotice?
    fun save(item: BrokerNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerNoticeStore(
    var items: MutableMap<String, BrokerNotice>,
    var order: MutableList<String>,
) : BrokerNoticeStore {
    override fun find(code: String): BrokerNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerNotice) {
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

    override fun list(): MutableList<BrokerNotice> {
        val out = mutableListOf<BrokerNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerNoticeStore(): BrokerNoticeStore {
    return MemoryBrokerNoticeStore(mutableMapOf<String, BrokerNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerNoticeStore(
    var inner: BrokerNoticeStore,
    var cache: TtlCache<BrokerNotice>,
    var hits: Int,
    var misses: Int,
) : BrokerNoticeStore {
    override fun find(code: String): BrokerNotice? {
        val hit = (cache.get(code) as BrokerNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerNotice> = inner.list()
}

fun newCachedBrokerNoticeStore(inner: BrokerNoticeStore, ttl: Int, clock: Clock): BrokerNoticeStore {
    return CachedBrokerNoticeStore(
        inner = inner,
        cache = newTtlCache<BrokerNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker notices. Empty fields match all. */
data class BrokerNoticeQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerNotice>): MutableList<BrokerNotice> {
        val matched = filter(items) { candidate: BrokerNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerNotices(store: BrokerNoticeStore, query: BrokerNoticeQuery): MutableList<BrokerNotice> {
    return query.apply(store.list())
}
