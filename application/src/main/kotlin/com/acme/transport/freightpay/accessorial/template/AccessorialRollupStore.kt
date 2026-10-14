package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial rollups by code. */
interface AccessorialRollupStore {
    fun find(code: String): AccessorialRollup?
    fun save(item: AccessorialRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialRollupStore(
    var items: MutableMap<String, AccessorialRollup>,
    var order: MutableList<String>,
) : AccessorialRollupStore {
    override fun find(code: String): AccessorialRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialRollup) {
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

    override fun list(): MutableList<AccessorialRollup> {
        val out = mutableListOf<AccessorialRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialRollupStore(): AccessorialRollupStore {
    return MemoryAccessorialRollupStore(mutableMapOf<String, AccessorialRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialRollupStore(
    var inner: AccessorialRollupStore,
    var cache: TtlCache<AccessorialRollup>,
    var hits: Int,
    var misses: Int,
) : AccessorialRollupStore {
    override fun find(code: String): AccessorialRollup? {
        val hit = (cache.get(code) as AccessorialRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialRollup> = inner.list()
}

fun newCachedAccessorialRollupStore(inner: AccessorialRollupStore, ttl: Int, clock: Clock): AccessorialRollupStore {
    return CachedAccessorialRollupStore(
        inner = inner,
        cache = newTtlCache<AccessorialRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial rollups. Empty fields match all. */
data class AccessorialRollupQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialRollup): Boolean {
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

    fun apply(items: MutableList<AccessorialRollup>): MutableList<AccessorialRollup> {
        val matched = filter(items) { candidate: AccessorialRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialRollups(store: AccessorialRollupStore, query: AccessorialRollupQuery): MutableList<AccessorialRollup> {
    return query.apply(store.list())
}
