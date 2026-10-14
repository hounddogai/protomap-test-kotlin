package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped violation thresholds by code. */
interface CappedViolationThresholdStore {
    fun find(code: String): CappedViolationThreshold?
    fun save(item: CappedViolationThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedViolationThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedViolationThresholdStore(
    var items: MutableMap<String, CappedViolationThreshold>,
    var order: MutableList<String>,
) : CappedViolationThresholdStore {
    override fun find(code: String): CappedViolationThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedViolationThreshold) {
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

    override fun list(): MutableList<CappedViolationThreshold> {
        val out = mutableListOf<CappedViolationThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedViolationThresholdStore(): CappedViolationThresholdStore {
    return MemoryCappedViolationThresholdStore(
        mutableMapOf<String, CappedViolationThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedViolationThresholdStore(
    var inner: CappedViolationThresholdStore,
    var cache: TtlCache<CappedViolationThreshold>,
    var hits: Int,
    var misses: Int,
) : CappedViolationThresholdStore {
    override fun find(code: String): CappedViolationThreshold? {
        val hit = (cache.get(code) as CappedViolationThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedViolationThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedViolationThreshold> = inner.list()
}

fun newCachedCappedViolationThresholdStore(inner: CappedViolationThresholdStore, ttl: Int, clock: Clock): CappedViolationThresholdStore {
    return CachedCappedViolationThresholdStore(
        inner = inner,
        cache = newTtlCache<CappedViolationThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped violation thresholds. Empty fields match all. */
data class CappedViolationThresholdQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedViolationThreshold): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedViolationThreshold>): MutableList<CappedViolationThreshold> {
        val matched = filter(items) { candidate: CappedViolationThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedViolationThresholds(store: CappedViolationThresholdStore, query: CappedViolationThresholdQuery): MutableList<CappedViolationThreshold> {
    return query.apply(store.list())
}
