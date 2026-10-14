package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict locker targets by code. */
interface StrictLockerTargetStore {
    fun find(code: String): StrictLockerTarget?
    fun save(item: StrictLockerTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLockerTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLockerTargetStore(
    var items: MutableMap<String, StrictLockerTarget>,
    var order: MutableList<String>,
) : StrictLockerTargetStore {
    override fun find(code: String): StrictLockerTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLockerTarget) {
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

    override fun list(): MutableList<StrictLockerTarget> {
        val out = mutableListOf<StrictLockerTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLockerTargetStore(): StrictLockerTargetStore {
    return MemoryStrictLockerTargetStore(mutableMapOf<String, StrictLockerTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLockerTargetStore(
    var inner: StrictLockerTargetStore,
    var cache: TtlCache<StrictLockerTarget>,
    var hits: Int,
    var misses: Int,
) : StrictLockerTargetStore {
    override fun find(code: String): StrictLockerTarget? {
        val hit = (cache.get(code) as StrictLockerTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLockerTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLockerTarget> = inner.list()
}

fun newCachedStrictLockerTargetStore(inner: StrictLockerTargetStore, ttl: Int, clock: Clock): StrictLockerTargetStore {
    return CachedStrictLockerTargetStore(
        inner = inner,
        cache = newTtlCache<StrictLockerTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict locker targets. Empty fields match all. */
data class StrictLockerTargetQuery(
    var unit: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLockerTarget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLockerTarget>): MutableList<StrictLockerTarget> {
        val matched = filter(items) { candidate: StrictLockerTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLockerTargets(store: StrictLockerTargetStore, query: StrictLockerTargetQuery): MutableList<StrictLockerTarget> {
    return query.apply(store.list())
}
