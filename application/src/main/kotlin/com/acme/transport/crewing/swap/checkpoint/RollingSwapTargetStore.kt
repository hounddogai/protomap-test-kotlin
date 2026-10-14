package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling swap targets by code. */
interface RollingSwapTargetStore {
    fun find(code: String): RollingSwapTarget?
    fun save(item: RollingSwapTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSwapTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSwapTargetStore(
    var items: MutableMap<String, RollingSwapTarget>,
    var order: MutableList<String>,
) : RollingSwapTargetStore {
    override fun find(code: String): RollingSwapTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSwapTarget) {
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

    override fun list(): MutableList<RollingSwapTarget> {
        val out = mutableListOf<RollingSwapTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSwapTargetStore(): RollingSwapTargetStore {
    return MemoryRollingSwapTargetStore(mutableMapOf<String, RollingSwapTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSwapTargetStore(
    var inner: RollingSwapTargetStore,
    var cache: TtlCache<RollingSwapTarget>,
    var hits: Int,
    var misses: Int,
) : RollingSwapTargetStore {
    override fun find(code: String): RollingSwapTarget? {
        val hit = (cache.get(code) as RollingSwapTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSwapTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSwapTarget> = inner.list()
}

fun newCachedRollingSwapTargetStore(inner: RollingSwapTargetStore, ttl: Int, clock: Clock): RollingSwapTargetStore {
    return CachedRollingSwapTargetStore(
        inner = inner,
        cache = newTtlCache<RollingSwapTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling swap targets. Empty fields match all. */
data class RollingSwapTargetQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSwapTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSwapTarget>): MutableList<RollingSwapTarget> {
        val matched = filter(items) { candidate: RollingSwapTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSwapTargets(store: RollingSwapTargetStore, query: RollingSwapTargetQuery): MutableList<RollingSwapTarget> {
    return query.apply(store.list())
}
