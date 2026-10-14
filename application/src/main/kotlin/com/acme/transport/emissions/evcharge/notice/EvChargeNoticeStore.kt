package com.acme.transport.emissions.evcharge.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge notices by code. */
interface EvChargeNoticeStore {
    fun find(code: String): EvChargeNotice?
    fun save(item: EvChargeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeNoticeStore(
    var items: MutableMap<String, EvChargeNotice>,
    var order: MutableList<String>,
) : EvChargeNoticeStore {
    override fun find(code: String): EvChargeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeNotice) {
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

    override fun list(): MutableList<EvChargeNotice> {
        val out = mutableListOf<EvChargeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeNoticeStore(): EvChargeNoticeStore {
    return MemoryEvChargeNoticeStore(mutableMapOf<String, EvChargeNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeNoticeStore(
    var inner: EvChargeNoticeStore,
    var cache: TtlCache<EvChargeNotice>,
    var hits: Int,
    var misses: Int,
) : EvChargeNoticeStore {
    override fun find(code: String): EvChargeNotice? {
        val hit = (cache.get(code) as EvChargeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeNotice> = inner.list()
}

fun newCachedEvChargeNoticeStore(inner: EvChargeNoticeStore, ttl: Int, clock: Clock): EvChargeNoticeStore {
    return CachedEvChargeNoticeStore(
        inner = inner,
        cache = newTtlCache<EvChargeNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge notices. Empty fields match all. */
data class EvChargeNoticeQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeNotice>): MutableList<EvChargeNotice> {
        val matched = filter(items) { candidate: EvChargeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeNotices(store: EvChargeNoticeStore, query: EvChargeNoticeQuery): MutableList<EvChargeNotice> {
    return query.apply(store.list())
}
