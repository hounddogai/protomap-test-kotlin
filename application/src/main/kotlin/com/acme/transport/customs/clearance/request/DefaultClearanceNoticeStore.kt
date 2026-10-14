package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default clearance notices by code. */
interface DefaultClearanceNoticeStore {
    fun find(code: String): DefaultClearanceNotice?
    fun save(item: DefaultClearanceNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultClearanceNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultClearanceNoticeStore(
    var items: MutableMap<String, DefaultClearanceNotice>,
    var order: MutableList<String>,
) : DefaultClearanceNoticeStore {
    override fun find(code: String): DefaultClearanceNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultClearanceNotice) {
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

    override fun list(): MutableList<DefaultClearanceNotice> {
        val out = mutableListOf<DefaultClearanceNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultClearanceNoticeStore(): DefaultClearanceNoticeStore {
    return MemoryDefaultClearanceNoticeStore(
        mutableMapOf<String, DefaultClearanceNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultClearanceNoticeStore(
    var inner: DefaultClearanceNoticeStore,
    var cache: TtlCache<DefaultClearanceNotice>,
    var hits: Int,
    var misses: Int,
) : DefaultClearanceNoticeStore {
    override fun find(code: String): DefaultClearanceNotice? {
        val hit = (cache.get(code) as DefaultClearanceNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultClearanceNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultClearanceNotice> = inner.list()
}

fun newCachedDefaultClearanceNoticeStore(inner: DefaultClearanceNoticeStore, ttl: Int, clock: Clock): DefaultClearanceNoticeStore {
    return CachedDefaultClearanceNoticeStore(
        inner = inner,
        cache = newTtlCache<DefaultClearanceNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default clearance notices. Empty fields match all. */
data class DefaultClearanceNoticeQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultClearanceNotice): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultClearanceNotice>): MutableList<DefaultClearanceNotice> {
        val matched = filter(items) { candidate: DefaultClearanceNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultClearanceNotices(store: DefaultClearanceNoticeStore, query: DefaultClearanceNoticeQuery): MutableList<DefaultClearanceNotice> {
    return query.apply(store.list())
}
