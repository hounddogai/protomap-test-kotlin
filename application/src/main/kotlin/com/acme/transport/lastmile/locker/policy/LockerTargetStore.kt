package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker targets by code. */
interface LockerTargetStore {
    fun find(code: String): LockerTarget?
    fun save(item: LockerTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerTargetStore(
    var items: MutableMap<String, LockerTarget>,
    var order: MutableList<String>,
) : LockerTargetStore {
    override fun find(code: String): LockerTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerTarget) {
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

    override fun list(): MutableList<LockerTarget> {
        val out = mutableListOf<LockerTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerTargetStore(): LockerTargetStore {
    return MemoryLockerTargetStore(mutableMapOf<String, LockerTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerTargetStore(
    var inner: LockerTargetStore,
    var cache: TtlCache<LockerTarget>,
    var hits: Int,
    var misses: Int,
) : LockerTargetStore {
    override fun find(code: String): LockerTarget? {
        val hit = (cache.get(code) as LockerTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerTarget> = inner.list()
}

fun newCachedLockerTargetStore(inner: LockerTargetStore, ttl: Int, clock: Clock): LockerTargetStore {
    return CachedLockerTargetStore(
        inner = inner,
        cache = newTtlCache<LockerTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker targets. Empty fields match all. */
data class LockerTargetQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerTarget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerTarget>): MutableList<LockerTarget> {
        val matched = filter(items) { candidate: LockerTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerTargets(store: LockerTargetStore, query: LockerTargetQuery): MutableList<LockerTarget> {
    return query.apply(store.list())
}
