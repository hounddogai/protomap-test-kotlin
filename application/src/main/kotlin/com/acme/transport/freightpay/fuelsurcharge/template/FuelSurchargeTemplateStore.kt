package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge templates by code. */
interface FuelSurchargeTemplateStore {
    fun find(code: String): FuelSurchargeTemplate?
    fun save(item: FuelSurchargeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeTemplateStore(
    var items: MutableMap<String, FuelSurchargeTemplate>,
    var order: MutableList<String>,
) : FuelSurchargeTemplateStore {
    override fun find(code: String): FuelSurchargeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeTemplate) {
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

    override fun list(): MutableList<FuelSurchargeTemplate> {
        val out = mutableListOf<FuelSurchargeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeTemplateStore(): FuelSurchargeTemplateStore {
    return MemoryFuelSurchargeTemplateStore(
        mutableMapOf<String, FuelSurchargeTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeTemplateStore(
    var inner: FuelSurchargeTemplateStore,
    var cache: TtlCache<FuelSurchargeTemplate>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeTemplateStore {
    override fun find(code: String): FuelSurchargeTemplate? {
        val hit = (cache.get(code) as FuelSurchargeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeTemplate> = inner.list()
}

fun newCachedFuelSurchargeTemplateStore(inner: FuelSurchargeTemplateStore, ttl: Int, clock: Clock): FuelSurchargeTemplateStore {
    return CachedFuelSurchargeTemplateStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge templates. Empty fields match all. */
data class FuelSurchargeTemplateQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeTemplate>): MutableList<FuelSurchargeTemplate> {
        val matched = filter(items) { candidate: FuelSurchargeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeTemplates(store: FuelSurchargeTemplateStore, query: FuelSurchargeTemplateQuery): MutableList<FuelSurchargeTemplate> {
    return query.apply(store.list())
}
