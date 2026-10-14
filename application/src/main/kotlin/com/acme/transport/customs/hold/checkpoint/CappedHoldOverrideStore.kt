package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped hold overrides by code. */
interface CappedHoldOverrideStore {
    fun find(code: String): CappedHoldOverride?
    fun save(item: CappedHoldOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedHoldOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedHoldOverrideStore(
    var items: MutableMap<String, CappedHoldOverride>,
    var order: MutableList<String>,
) : CappedHoldOverrideStore {
    override fun find(code: String): CappedHoldOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedHoldOverride) {
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

    override fun list(): MutableList<CappedHoldOverride> {
        val out = mutableListOf<CappedHoldOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedHoldOverrideStore(): CappedHoldOverrideStore {
    return MemoryCappedHoldOverrideStore(mutableMapOf<String, CappedHoldOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedHoldOverrideStore(
    var inner: CappedHoldOverrideStore,
    var cache: TtlCache<CappedHoldOverride>,
    var hits: Int,
    var misses: Int,
) : CappedHoldOverrideStore {
    override fun find(code: String): CappedHoldOverride? {
        val hit = (cache.get(code) as CappedHoldOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedHoldOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedHoldOverride> = inner.list()
}

fun newCachedCappedHoldOverrideStore(inner: CappedHoldOverrideStore, ttl: Int, clock: Clock): CappedHoldOverrideStore {
    return CachedCappedHoldOverrideStore(
        inner = inner,
        cache = newTtlCache<CappedHoldOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped hold overrides. Empty fields match all. */
data class CappedHoldOverrideQuery(
    var segment: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedHoldOverride): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedHoldOverride>): MutableList<CappedHoldOverride> {
        val matched = filter(items) { candidate: CappedHoldOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedHoldOverrides(store: CappedHoldOverrideStore, query: CappedHoldOverrideQuery): MutableList<CappedHoldOverride> {
    return query.apply(store.list())
}
