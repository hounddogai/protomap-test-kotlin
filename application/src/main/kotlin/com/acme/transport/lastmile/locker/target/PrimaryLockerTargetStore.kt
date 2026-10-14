package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary locker targets by code. */
interface PrimaryLockerTargetStore {
    fun find(code: String): PrimaryLockerTarget?
    fun save(item: PrimaryLockerTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryLockerTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryLockerTargetStore(
    var items: MutableMap<String, PrimaryLockerTarget>,
    var order: MutableList<String>,
) : PrimaryLockerTargetStore {
    override fun find(code: String): PrimaryLockerTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryLockerTarget) {
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

    override fun list(): MutableList<PrimaryLockerTarget> {
        val out = mutableListOf<PrimaryLockerTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryLockerTargetStore(): PrimaryLockerTargetStore {
    return MemoryPrimaryLockerTargetStore(mutableMapOf<String, PrimaryLockerTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryLockerTargetStore(
    var inner: PrimaryLockerTargetStore,
    var cache: TtlCache<PrimaryLockerTarget>,
    var hits: Int,
    var misses: Int,
) : PrimaryLockerTargetStore {
    override fun find(code: String): PrimaryLockerTarget? {
        val hit = (cache.get(code) as PrimaryLockerTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryLockerTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryLockerTarget> = inner.list()
}

fun newCachedPrimaryLockerTargetStore(inner: PrimaryLockerTargetStore, ttl: Int, clock: Clock): PrimaryLockerTargetStore {
    return CachedPrimaryLockerTargetStore(
        inner = inner,
        cache = newTtlCache<PrimaryLockerTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary locker targets. Empty fields match all. */
data class PrimaryLockerTargetQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryLockerTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryLockerTarget>): MutableList<PrimaryLockerTarget> {
        val matched = filter(items) { candidate: PrimaryLockerTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryLockerTargets(store: PrimaryLockerTargetStore, query: PrimaryLockerTargetQuery): MutableList<PrimaryLockerTarget> {
    return query.apply(store.list())
}
