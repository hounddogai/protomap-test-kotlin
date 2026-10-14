package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit notices by code. */
interface AuditNoticeStore {
    fun find(code: String): AuditNotice?
    fun save(item: AuditNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditNoticeStore(
    var items: MutableMap<String, AuditNotice>,
    var order: MutableList<String>,
) : AuditNoticeStore {
    override fun find(code: String): AuditNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditNotice) {
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

    override fun list(): MutableList<AuditNotice> {
        val out = mutableListOf<AuditNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditNoticeStore(): AuditNoticeStore {
    return MemoryAuditNoticeStore(mutableMapOf<String, AuditNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditNoticeStore(
    var inner: AuditNoticeStore,
    var cache: TtlCache<AuditNotice>,
    var hits: Int,
    var misses: Int,
) : AuditNoticeStore {
    override fun find(code: String): AuditNotice? {
        val hit = (cache.get(code) as AuditNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditNotice> = inner.list()
}

fun newCachedAuditNoticeStore(inner: AuditNoticeStore, ttl: Int, clock: Clock): AuditNoticeStore {
    return CachedAuditNoticeStore(
        inner = inner,
        cache = newTtlCache<AuditNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit notices. Empty fields match all. */
data class AuditNoticeQuery(
    var source: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditNotice): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<AuditNotice>): MutableList<AuditNotice> {
        val matched = filter(items) { candidate: AuditNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditNotices(store: AuditNoticeStore, query: AuditNoticeQuery): MutableList<AuditNotice> {
    return query.apply(store.list())
}
