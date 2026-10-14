package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery targets by code. */
interface RedeliveryTargetStore {
    fun find(code: String): RedeliveryTarget?
    fun save(item: RedeliveryTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryTargetStore(
    var items: MutableMap<String, RedeliveryTarget>,
    var order: MutableList<String>,
) : RedeliveryTargetStore {
    override fun find(code: String): RedeliveryTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryTarget) {
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

    override fun list(): MutableList<RedeliveryTarget> {
        val out = mutableListOf<RedeliveryTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryTargetStore(): RedeliveryTargetStore {
    return MemoryRedeliveryTargetStore(mutableMapOf<String, RedeliveryTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryTargetStore(
    var inner: RedeliveryTargetStore,
    var cache: TtlCache<RedeliveryTarget>,
    var hits: Int,
    var misses: Int,
) : RedeliveryTargetStore {
    override fun find(code: String): RedeliveryTarget? {
        val hit = (cache.get(code) as RedeliveryTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryTarget> = inner.list()
}

fun newCachedRedeliveryTargetStore(inner: RedeliveryTargetStore, ttl: Int, clock: Clock): RedeliveryTargetStore {
    return CachedRedeliveryTargetStore(
        inner = inner,
        cache = newTtlCache<RedeliveryTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery targets. Empty fields match all. */
data class RedeliveryTargetQuery(
    var country: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryTarget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryTarget>): MutableList<RedeliveryTarget> {
        val matched = filter(items) { candidate: RedeliveryTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryTargets(store: RedeliveryTargetStore, query: RedeliveryTargetQuery): MutableList<RedeliveryTarget> {
    return query.apply(store.list())
}
