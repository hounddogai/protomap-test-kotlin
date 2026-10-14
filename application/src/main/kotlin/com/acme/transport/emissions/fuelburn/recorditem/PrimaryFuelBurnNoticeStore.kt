package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary fuel burn notices by code. */
interface PrimaryFuelBurnNoticeStore {
    fun find(code: String): PrimaryFuelBurnNotice?
    fun save(item: PrimaryFuelBurnNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFuelBurnNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFuelBurnNoticeStore(
    var items: MutableMap<String, PrimaryFuelBurnNotice>,
    var order: MutableList<String>,
) : PrimaryFuelBurnNoticeStore {
    override fun find(code: String): PrimaryFuelBurnNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFuelBurnNotice) {
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

    override fun list(): MutableList<PrimaryFuelBurnNotice> {
        val out = mutableListOf<PrimaryFuelBurnNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFuelBurnNoticeStore(): PrimaryFuelBurnNoticeStore {
    return MemoryPrimaryFuelBurnNoticeStore(
        mutableMapOf<String, PrimaryFuelBurnNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFuelBurnNoticeStore(
    var inner: PrimaryFuelBurnNoticeStore,
    var cache: TtlCache<PrimaryFuelBurnNotice>,
    var hits: Int,
    var misses: Int,
) : PrimaryFuelBurnNoticeStore {
    override fun find(code: String): PrimaryFuelBurnNotice? {
        val hit = (cache.get(code) as PrimaryFuelBurnNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFuelBurnNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFuelBurnNotice> = inner.list()
}

fun newCachedPrimaryFuelBurnNoticeStore(inner: PrimaryFuelBurnNoticeStore, ttl: Int, clock: Clock): PrimaryFuelBurnNoticeStore {
    return CachedPrimaryFuelBurnNoticeStore(
        inner = inner,
        cache = newTtlCache<PrimaryFuelBurnNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary fuel burn notices. Empty fields match all. */
data class PrimaryFuelBurnNoticeQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFuelBurnNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryFuelBurnNotice>): MutableList<PrimaryFuelBurnNotice> {
        val matched = filter(items) { candidate: PrimaryFuelBurnNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFuelBurnNotices(store: PrimaryFuelBurnNoticeStore, query: PrimaryFuelBurnNoticeQuery): MutableList<PrimaryFuelBurnNotice> {
    return query.apply(store.list())
}
