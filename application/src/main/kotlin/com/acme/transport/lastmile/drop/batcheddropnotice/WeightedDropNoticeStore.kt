package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted drop notices by code. */
interface WeightedDropNoticeStore {
    fun find(code: String): WeightedDropNotice?
    fun save(item: WeightedDropNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDropNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDropNoticeStore(
    var items: MutableMap<String, WeightedDropNotice>,
    var order: MutableList<String>,
) : WeightedDropNoticeStore {
    override fun find(code: String): WeightedDropNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDropNotice) {
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

    override fun list(): MutableList<WeightedDropNotice> {
        val out = mutableListOf<WeightedDropNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDropNoticeStore(): WeightedDropNoticeStore {
    return MemoryWeightedDropNoticeStore(mutableMapOf<String, WeightedDropNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDropNoticeStore(
    var inner: WeightedDropNoticeStore,
    var cache: TtlCache<WeightedDropNotice>,
    var hits: Int,
    var misses: Int,
) : WeightedDropNoticeStore {
    override fun find(code: String): WeightedDropNotice? {
        val hit = (cache.get(code) as WeightedDropNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDropNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDropNotice> = inner.list()
}

fun newCachedWeightedDropNoticeStore(inner: WeightedDropNoticeStore, ttl: Int, clock: Clock): WeightedDropNoticeStore {
    return CachedWeightedDropNoticeStore(
        inner = inner,
        cache = newTtlCache<WeightedDropNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted drop notices. Empty fields match all. */
data class WeightedDropNoticeQuery(
    var region: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDropNotice): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<WeightedDropNotice>): MutableList<WeightedDropNotice> {
        val matched = filter(items) { candidate: WeightedDropNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDropNotices(store: WeightedDropNoticeStore, query: WeightedDropNoticeQuery): MutableList<WeightedDropNotice> {
    return query.apply(store.list())
}
