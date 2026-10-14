package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped relay notices by code. */
interface CappedRelayNoticeStore {
    fun find(code: String): CappedRelayNotice?
    fun save(item: CappedRelayNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRelayNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRelayNoticeStore(
    var items: MutableMap<String, CappedRelayNotice>,
    var order: MutableList<String>,
) : CappedRelayNoticeStore {
    override fun find(code: String): CappedRelayNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRelayNotice) {
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

    override fun list(): MutableList<CappedRelayNotice> {
        val out = mutableListOf<CappedRelayNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRelayNoticeStore(): CappedRelayNoticeStore {
    return MemoryCappedRelayNoticeStore(mutableMapOf<String, CappedRelayNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRelayNoticeStore(
    var inner: CappedRelayNoticeStore,
    var cache: TtlCache<CappedRelayNotice>,
    var hits: Int,
    var misses: Int,
) : CappedRelayNoticeStore {
    override fun find(code: String): CappedRelayNotice? {
        val hit = (cache.get(code) as CappedRelayNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRelayNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRelayNotice> = inner.list()
}

fun newCachedCappedRelayNoticeStore(inner: CappedRelayNoticeStore, ttl: Int, clock: Clock): CappedRelayNoticeStore {
    return CachedCappedRelayNoticeStore(
        inner = inner,
        cache = newTtlCache<CappedRelayNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped relay notices. Empty fields match all. */
data class CappedRelayNoticeQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRelayNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRelayNotice>): MutableList<CappedRelayNotice> {
        val matched = filter(items) { candidate: CappedRelayNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRelayNotices(store: CappedRelayNoticeStore, query: CappedRelayNoticeQuery): MutableList<CappedRelayNotice> {
    return query.apply(store.list())
}
