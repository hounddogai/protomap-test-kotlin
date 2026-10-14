package com.acme.transport.customs.clearance.target

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance targets by code. */
interface ClearanceTargetStore {
    fun find(code: String): ClearanceTarget?
    fun save(item: ClearanceTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceTargetStore(
    var items: MutableMap<String, ClearanceTarget>,
    var order: MutableList<String>,
) : ClearanceTargetStore {
    override fun find(code: String): ClearanceTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceTarget) {
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

    override fun list(): MutableList<ClearanceTarget> {
        val out = mutableListOf<ClearanceTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceTargetStore(): ClearanceTargetStore {
    return MemoryClearanceTargetStore(mutableMapOf<String, ClearanceTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceTargetStore(
    var inner: ClearanceTargetStore,
    var cache: TtlCache<ClearanceTarget>,
    var hits: Int,
    var misses: Int,
) : ClearanceTargetStore {
    override fun find(code: String): ClearanceTarget? {
        val hit = (cache.get(code) as ClearanceTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceTarget> = inner.list()
}

fun newCachedClearanceTargetStore(inner: ClearanceTargetStore, ttl: Int, clock: Clock): ClearanceTargetStore {
    return CachedClearanceTargetStore(
        inner = inner,
        cache = newTtlCache<ClearanceTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance targets. Empty fields match all. */
data class ClearanceTargetQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceTarget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<ClearanceTarget>): MutableList<ClearanceTarget> {
        val matched = filter(items) { candidate: ClearanceTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceTargets(store: ClearanceTargetStore, query: ClearanceTargetQuery): MutableList<ClearanceTarget> {
    return query.apply(store.list())
}
