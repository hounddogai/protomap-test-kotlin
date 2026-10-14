package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback violation adjustments by code. */
interface FallbackViolationAdjustmentStore {
    fun find(code: String): FallbackViolationAdjustment?
    fun save(item: FallbackViolationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackViolationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackViolationAdjustmentStore(
    var items: MutableMap<String, FallbackViolationAdjustment>,
    var order: MutableList<String>,
) : FallbackViolationAdjustmentStore {
    override fun find(code: String): FallbackViolationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackViolationAdjustment) {
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

    override fun list(): MutableList<FallbackViolationAdjustment> {
        val out = mutableListOf<FallbackViolationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackViolationAdjustmentStore(): FallbackViolationAdjustmentStore {
    return MemoryFallbackViolationAdjustmentStore(
        mutableMapOf<String, FallbackViolationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackViolationAdjustmentStore(
    var inner: FallbackViolationAdjustmentStore,
    var cache: TtlCache<FallbackViolationAdjustment>,
    var hits: Int,
    var misses: Int,
) : FallbackViolationAdjustmentStore {
    override fun find(code: String): FallbackViolationAdjustment? {
        val hit = (cache.get(code) as FallbackViolationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackViolationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackViolationAdjustment> = inner.list()
}

fun newCachedFallbackViolationAdjustmentStore(inner: FallbackViolationAdjustmentStore, ttl: Int, clock: Clock): FallbackViolationAdjustmentStore {
    return CachedFallbackViolationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<FallbackViolationAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback violation adjustments. Empty fields match all. */
data class FallbackViolationAdjustmentQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackViolationAdjustment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackViolationAdjustment>): MutableList<FallbackViolationAdjustment> {
        val matched = filter(items) { candidate: FallbackViolationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackViolationAdjustments(store: FallbackViolationAdjustmentStore, query: FallbackViolationAdjustmentQuery): MutableList<FallbackViolationAdjustment> {
    return query.apply(store.list())
}
