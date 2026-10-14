package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled rest break targets by code. */
interface ScheduledRestBreakTargetStore {
    fun find(code: String): ScheduledRestBreakTarget?
    fun save(item: ScheduledRestBreakTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRestBreakTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRestBreakTargetStore(
    var items: MutableMap<String, ScheduledRestBreakTarget>,
    var order: MutableList<String>,
) : ScheduledRestBreakTargetStore {
    override fun find(code: String): ScheduledRestBreakTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRestBreakTarget) {
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

    override fun list(): MutableList<ScheduledRestBreakTarget> {
        val out = mutableListOf<ScheduledRestBreakTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRestBreakTargetStore(): ScheduledRestBreakTargetStore {
    return MemoryScheduledRestBreakTargetStore(
        mutableMapOf<String, ScheduledRestBreakTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRestBreakTargetStore(
    var inner: ScheduledRestBreakTargetStore,
    var cache: TtlCache<ScheduledRestBreakTarget>,
    var hits: Int,
    var misses: Int,
) : ScheduledRestBreakTargetStore {
    override fun find(code: String): ScheduledRestBreakTarget? {
        val hit = (cache.get(code) as ScheduledRestBreakTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRestBreakTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRestBreakTarget> = inner.list()
}

fun newCachedScheduledRestBreakTargetStore(inner: ScheduledRestBreakTargetStore, ttl: Int, clock: Clock): ScheduledRestBreakTargetStore {
    return CachedScheduledRestBreakTargetStore(
        inner = inner,
        cache = newTtlCache<ScheduledRestBreakTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled rest break targets. Empty fields match all. */
data class ScheduledRestBreakTargetQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRestBreakTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRestBreakTarget>): MutableList<ScheduledRestBreakTarget> {
        val matched = filter(items) { candidate: ScheduledRestBreakTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRestBreakTargets(store: ScheduledRestBreakTargetStore, query: ScheduledRestBreakTargetQuery): MutableList<ScheduledRestBreakTarget> {
    return query.apply(store.list())
}
