package com.acme.transport.freightpay.audit.batch

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default audit notices by code. */
interface DefaultAuditNoticeStore {
    fun find(code: String): DefaultAuditNotice?
    fun save(item: DefaultAuditNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAuditNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAuditNoticeStore(
    var items: MutableMap<String, DefaultAuditNotice>,
    var order: MutableList<String>,
) : DefaultAuditNoticeStore {
    override fun find(code: String): DefaultAuditNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAuditNotice) {
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

    override fun list(): MutableList<DefaultAuditNotice> {
        val out = mutableListOf<DefaultAuditNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAuditNoticeStore(): DefaultAuditNoticeStore {
    return MemoryDefaultAuditNoticeStore(mutableMapOf<String, DefaultAuditNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAuditNoticeStore(
    var inner: DefaultAuditNoticeStore,
    var cache: TtlCache<DefaultAuditNotice>,
    var hits: Int,
    var misses: Int,
) : DefaultAuditNoticeStore {
    override fun find(code: String): DefaultAuditNotice? {
        val hit = (cache.get(code) as DefaultAuditNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAuditNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAuditNotice> = inner.list()
}

fun newCachedDefaultAuditNoticeStore(inner: DefaultAuditNoticeStore, ttl: Int, clock: Clock): DefaultAuditNoticeStore {
    return CachedDefaultAuditNoticeStore(
        inner = inner,
        cache = newTtlCache<DefaultAuditNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default audit notices. Empty fields match all. */
data class DefaultAuditNoticeQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAuditNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAuditNotice>): MutableList<DefaultAuditNotice> {
        val matched = filter(items) { candidate: DefaultAuditNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAuditNotices(store: DefaultAuditNoticeStore, query: DefaultAuditNoticeQuery): MutableList<DefaultAuditNotice> {
    return query.apply(store.list())
}
