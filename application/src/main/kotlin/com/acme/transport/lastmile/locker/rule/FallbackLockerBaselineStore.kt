package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback locker baselines by code. */
interface FallbackLockerBaselineStore {
    fun find(code: String): FallbackLockerBaseline?
    fun save(item: FallbackLockerBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLockerBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLockerBaselineStore(
    var items: MutableMap<String, FallbackLockerBaseline>,
    var order: MutableList<String>,
) : FallbackLockerBaselineStore {
    override fun find(code: String): FallbackLockerBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLockerBaseline) {
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

    override fun list(): MutableList<FallbackLockerBaseline> {
        val out = mutableListOf<FallbackLockerBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLockerBaselineStore(): FallbackLockerBaselineStore {
    return MemoryFallbackLockerBaselineStore(
        mutableMapOf<String, FallbackLockerBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLockerBaselineStore(
    var inner: FallbackLockerBaselineStore,
    var cache: TtlCache<FallbackLockerBaseline>,
    var hits: Int,
    var misses: Int,
) : FallbackLockerBaselineStore {
    override fun find(code: String): FallbackLockerBaseline? {
        val hit = (cache.get(code) as FallbackLockerBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLockerBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLockerBaseline> = inner.list()
}

fun newCachedFallbackLockerBaselineStore(inner: FallbackLockerBaselineStore, ttl: Int, clock: Clock): FallbackLockerBaselineStore {
    return CachedFallbackLockerBaselineStore(
        inner = inner,
        cache = newTtlCache<FallbackLockerBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback locker baselines. Empty fields match all. */
data class FallbackLockerBaselineQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLockerBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLockerBaseline>): MutableList<FallbackLockerBaseline> {
        val matched = filter(items) { candidate: FallbackLockerBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLockerBaselines(store: FallbackLockerBaselineStore, query: FallbackLockerBaselineQuery): MutableList<FallbackLockerBaseline> {
    return query.apply(store.list())
}
