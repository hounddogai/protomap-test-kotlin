package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial notices by code. */
interface AccessorialNoticeStore {
    fun find(code: String): AccessorialNotice?
    fun save(item: AccessorialNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialNoticeStore(
    var items: MutableMap<String, AccessorialNotice>,
    var order: MutableList<String>,
) : AccessorialNoticeStore {
    override fun find(code: String): AccessorialNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialNotice) {
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

    override fun list(): MutableList<AccessorialNotice> {
        val out = mutableListOf<AccessorialNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialNoticeStore(): AccessorialNoticeStore {
    return MemoryAccessorialNoticeStore(mutableMapOf<String, AccessorialNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialNoticeStore(
    var inner: AccessorialNoticeStore,
    var cache: TtlCache<AccessorialNotice>,
    var hits: Int,
    var misses: Int,
) : AccessorialNoticeStore {
    override fun find(code: String): AccessorialNotice? {
        val hit = (cache.get(code) as AccessorialNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialNotice> = inner.list()
}

fun newCachedAccessorialNoticeStore(inner: AccessorialNoticeStore, ttl: Int, clock: Clock): AccessorialNoticeStore {
    return CachedAccessorialNoticeStore(
        inner = inner,
        cache = newTtlCache<AccessorialNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial notices. Empty fields match all. */
data class AccessorialNoticeQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialNotice): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialNotice>): MutableList<AccessorialNotice> {
        val matched = filter(items) { candidate: AccessorialNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialNotices(store: AccessorialNoticeStore, query: AccessorialNoticeQuery): MutableList<AccessorialNotice> {
    return query.apply(store.list())
}
