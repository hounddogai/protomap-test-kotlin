package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender baselines by code. */
interface TenderBaselineStore {
    fun find(code: String): TenderBaseline?
    fun save(item: TenderBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderBaselineStore(
    var items: MutableMap<String, TenderBaseline>,
    var order: MutableList<String>,
) : TenderBaselineStore {
    override fun find(code: String): TenderBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderBaseline) {
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

    override fun list(): MutableList<TenderBaseline> {
        val out = mutableListOf<TenderBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderBaselineStore(): TenderBaselineStore {
    return MemoryTenderBaselineStore(mutableMapOf<String, TenderBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderBaselineStore(
    var inner: TenderBaselineStore,
    var cache: TtlCache<TenderBaseline>,
    var hits: Int,
    var misses: Int,
) : TenderBaselineStore {
    override fun find(code: String): TenderBaseline? {
        val hit = (cache.get(code) as TenderBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderBaseline> = inner.list()
}

fun newCachedTenderBaselineStore(inner: TenderBaselineStore, ttl: Int, clock: Clock): TenderBaselineStore {
    return CachedTenderBaselineStore(
        inner = inner,
        cache = newTtlCache<TenderBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender baselines. Empty fields match all. */
data class TenderBaselineQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderBaseline>): MutableList<TenderBaseline> {
        val matched = filter(items) { candidate: TenderBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderBaselines(store: TenderBaselineStore, query: TenderBaselineQuery): MutableList<TenderBaseline> {
    return query.apply(store.list())
}
