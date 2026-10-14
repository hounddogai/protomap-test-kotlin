package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold targets by code. */
interface HoldTargetStore {
    fun find(code: String): HoldTarget?
    fun save(item: HoldTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldTargetStore(
    var items: MutableMap<String, HoldTarget>,
    var order: MutableList<String>,
) : HoldTargetStore {
    override fun find(code: String): HoldTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldTarget) {
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

    override fun list(): MutableList<HoldTarget> {
        val out = mutableListOf<HoldTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldTargetStore(): HoldTargetStore {
    return MemoryHoldTargetStore(mutableMapOf<String, HoldTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldTargetStore(
    var inner: HoldTargetStore,
    var cache: TtlCache<HoldTarget>,
    var hits: Int,
    var misses: Int,
) : HoldTargetStore {
    override fun find(code: String): HoldTarget? {
        val hit = (cache.get(code) as HoldTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldTarget> = inner.list()
}

fun newCachedHoldTargetStore(inner: HoldTargetStore, ttl: Int, clock: Clock): HoldTargetStore {
    return CachedHoldTargetStore(
        inner = inner,
        cache = newTtlCache<HoldTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold targets. Empty fields match all. */
data class HoldTargetQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldTarget>): MutableList<HoldTarget> {
        val matched = filter(items) { candidate: HoldTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldTargets(store: HoldTargetStore, query: HoldTargetQuery): MutableList<HoldTarget> {
    return query.apply(store.list())
}
