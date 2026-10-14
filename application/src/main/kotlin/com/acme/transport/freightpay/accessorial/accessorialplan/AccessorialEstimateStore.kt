package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial estimates by code. */
interface AccessorialEstimateStore {
    fun find(code: String): AccessorialEstimate?
    fun save(item: AccessorialEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialEstimateStore(
    var items: MutableMap<String, AccessorialEstimate>,
    var order: MutableList<String>,
) : AccessorialEstimateStore {
    override fun find(code: String): AccessorialEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialEstimate) {
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

    override fun list(): MutableList<AccessorialEstimate> {
        val out = mutableListOf<AccessorialEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialEstimateStore(): AccessorialEstimateStore {
    return MemoryAccessorialEstimateStore(mutableMapOf<String, AccessorialEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialEstimateStore(
    var inner: AccessorialEstimateStore,
    var cache: TtlCache<AccessorialEstimate>,
    var hits: Int,
    var misses: Int,
) : AccessorialEstimateStore {
    override fun find(code: String): AccessorialEstimate? {
        val hit = (cache.get(code) as AccessorialEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialEstimate> = inner.list()
}

fun newCachedAccessorialEstimateStore(inner: AccessorialEstimateStore, ttl: Int, clock: Clock): AccessorialEstimateStore {
    return CachedAccessorialEstimateStore(
        inner = inner,
        cache = newTtlCache<AccessorialEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial estimates. Empty fields match all. */
data class AccessorialEstimateQuery(
    var channel: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialEstimate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialEstimate>): MutableList<AccessorialEstimate> {
        val matched = filter(items) { candidate: AccessorialEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialEstimates(store: AccessorialEstimateStore, query: AccessorialEstimateQuery): MutableList<AccessorialEstimate> {
    return query.apply(store.list())
}
