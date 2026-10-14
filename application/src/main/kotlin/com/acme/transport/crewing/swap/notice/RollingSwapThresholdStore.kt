package com.acme.transport.crewing.swap.notice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling swap thresholds by code. */
interface RollingSwapThresholdStore {
    fun find(code: String): RollingSwapThreshold?
    fun save(item: RollingSwapThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSwapThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSwapThresholdStore(
    var items: MutableMap<String, RollingSwapThreshold>,
    var order: MutableList<String>,
) : RollingSwapThresholdStore {
    override fun find(code: String): RollingSwapThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSwapThreshold) {
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

    override fun list(): MutableList<RollingSwapThreshold> {
        val out = mutableListOf<RollingSwapThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSwapThresholdStore(): RollingSwapThresholdStore {
    return MemoryRollingSwapThresholdStore(
        mutableMapOf<String, RollingSwapThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSwapThresholdStore(
    var inner: RollingSwapThresholdStore,
    var cache: TtlCache<RollingSwapThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingSwapThresholdStore {
    override fun find(code: String): RollingSwapThreshold? {
        val hit = (cache.get(code) as RollingSwapThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSwapThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSwapThreshold> = inner.list()
}

fun newCachedRollingSwapThresholdStore(inner: RollingSwapThresholdStore, ttl: Int, clock: Clock): RollingSwapThresholdStore {
    return CachedRollingSwapThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingSwapThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling swap thresholds. Empty fields match all. */
data class RollingSwapThresholdQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSwapThreshold): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSwapThreshold>): MutableList<RollingSwapThreshold> {
        val matched = filter(items) { candidate: RollingSwapThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSwapThresholds(store: RollingSwapThresholdStore, query: RollingSwapThresholdQuery): MutableList<RollingSwapThreshold> {
    return query.apply(store.list())
}
