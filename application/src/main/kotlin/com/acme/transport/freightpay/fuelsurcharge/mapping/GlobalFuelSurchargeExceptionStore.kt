package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel surcharge exceptions by code. */
interface GlobalFuelSurchargeExceptionStore {
    fun find(code: String): GlobalFuelSurchargeException?
    fun save(item: GlobalFuelSurchargeException)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelSurchargeException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelSurchargeExceptionStore(
    var items: MutableMap<String, GlobalFuelSurchargeException>,
    var order: MutableList<String>,
) : GlobalFuelSurchargeExceptionStore {
    override fun find(code: String): GlobalFuelSurchargeException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelSurchargeException) {
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

    override fun list(): MutableList<GlobalFuelSurchargeException> {
        val out = mutableListOf<GlobalFuelSurchargeException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelSurchargeExceptionStore(): GlobalFuelSurchargeExceptionStore {
    return MemoryGlobalFuelSurchargeExceptionStore(
        mutableMapOf<String, GlobalFuelSurchargeException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelSurchargeExceptionStore(
    var inner: GlobalFuelSurchargeExceptionStore,
    var cache: TtlCache<GlobalFuelSurchargeException>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelSurchargeExceptionStore {
    override fun find(code: String): GlobalFuelSurchargeException? {
        val hit = (cache.get(code) as GlobalFuelSurchargeException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelSurchargeException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelSurchargeException> = inner.list()
}

fun newCachedGlobalFuelSurchargeExceptionStore(inner: GlobalFuelSurchargeExceptionStore, ttl: Int, clock: Clock): GlobalFuelSurchargeExceptionStore {
    return CachedGlobalFuelSurchargeExceptionStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelSurchargeException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel surcharge exceptions. Empty fields match all. */
data class GlobalFuelSurchargeExceptionQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelSurchargeException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFuelSurchargeException>): MutableList<GlobalFuelSurchargeException> {
        val matched = filter(items) { candidate: GlobalFuelSurchargeException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelSurchargeExceptions(store: GlobalFuelSurchargeExceptionStore, query: GlobalFuelSurchargeExceptionQuery): MutableList<GlobalFuelSurchargeException> {
    return query.apply(store.list())
}
