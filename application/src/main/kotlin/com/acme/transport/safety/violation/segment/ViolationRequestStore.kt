package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation requests by code. */
interface ViolationRequestStore {
    fun find(code: String): ViolationRequest?
    fun save(item: ViolationRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationRequestStore(
    var items: MutableMap<String, ViolationRequest>,
    var order: MutableList<String>,
) : ViolationRequestStore {
    override fun find(code: String): ViolationRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationRequest) {
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

    override fun list(): MutableList<ViolationRequest> {
        val out = mutableListOf<ViolationRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationRequestStore(): ViolationRequestStore {
    return MemoryViolationRequestStore(mutableMapOf<String, ViolationRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationRequestStore(
    var inner: ViolationRequestStore,
    var cache: TtlCache<ViolationRequest>,
    var hits: Int,
    var misses: Int,
) : ViolationRequestStore {
    override fun find(code: String): ViolationRequest? {
        val hit = (cache.get(code) as ViolationRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationRequest> = inner.list()
}

fun newCachedViolationRequestStore(inner: ViolationRequestStore, ttl: Int, clock: Clock): ViolationRequestStore {
    return CachedViolationRequestStore(
        inner = inner,
        cache = newTtlCache<ViolationRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation requests. Empty fields match all. */
data class ViolationRequestQuery(
    var category: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationRequest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationRequest>): MutableList<ViolationRequest> {
        val matched = filter(items) { candidate: ViolationRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationRequests(store: ViolationRequestStore, query: ViolationRequestQuery): MutableList<ViolationRequest> {
    return query.apply(store.list())
}
