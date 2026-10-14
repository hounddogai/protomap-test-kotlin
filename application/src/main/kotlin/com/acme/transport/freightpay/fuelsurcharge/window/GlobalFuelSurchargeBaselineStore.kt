package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel surcharge baselines by code. */
interface GlobalFuelSurchargeBaselineStore {
    fun find(code: String): GlobalFuelSurchargeBaseline?
    fun save(item: GlobalFuelSurchargeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelSurchargeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelSurchargeBaselineStore(
    var items: MutableMap<String, GlobalFuelSurchargeBaseline>,
    var order: MutableList<String>,
) : GlobalFuelSurchargeBaselineStore {
    override fun find(code: String): GlobalFuelSurchargeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelSurchargeBaseline) {
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

    override fun list(): MutableList<GlobalFuelSurchargeBaseline> {
        val out = mutableListOf<GlobalFuelSurchargeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelSurchargeBaselineStore(): GlobalFuelSurchargeBaselineStore {
    return MemoryGlobalFuelSurchargeBaselineStore(
        mutableMapOf<String, GlobalFuelSurchargeBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelSurchargeBaselineStore(
    var inner: GlobalFuelSurchargeBaselineStore,
    var cache: TtlCache<GlobalFuelSurchargeBaseline>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelSurchargeBaselineStore {
    override fun find(code: String): GlobalFuelSurchargeBaseline? {
        val hit = (cache.get(code) as GlobalFuelSurchargeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelSurchargeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelSurchargeBaseline> = inner.list()
}

fun newCachedGlobalFuelSurchargeBaselineStore(inner: GlobalFuelSurchargeBaselineStore, ttl: Int, clock: Clock): GlobalFuelSurchargeBaselineStore {
    return CachedGlobalFuelSurchargeBaselineStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelSurchargeBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel surcharge baselines. Empty fields match all. */
data class GlobalFuelSurchargeBaselineQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelSurchargeBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<GlobalFuelSurchargeBaseline>): MutableList<GlobalFuelSurchargeBaseline> {
        val matched = filter(items) { candidate: GlobalFuelSurchargeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelSurchargeBaselines(store: GlobalFuelSurchargeBaselineStore, query: GlobalFuelSurchargeBaselineQuery): MutableList<GlobalFuelSurchargeBaseline> {
    return query.apply(store.list())
}
