package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling defect targets by code. */
interface RollingDefectTargetStore {
    fun find(code: String): RollingDefectTarget?
    fun save(item: RollingDefectTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDefectTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDefectTargetStore(
    var items: MutableMap<String, RollingDefectTarget>,
    var order: MutableList<String>,
) : RollingDefectTargetStore {
    override fun find(code: String): RollingDefectTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDefectTarget) {
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

    override fun list(): MutableList<RollingDefectTarget> {
        val out = mutableListOf<RollingDefectTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDefectTargetStore(): RollingDefectTargetStore {
    return MemoryRollingDefectTargetStore(mutableMapOf<String, RollingDefectTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDefectTargetStore(
    var inner: RollingDefectTargetStore,
    var cache: TtlCache<RollingDefectTarget>,
    var hits: Int,
    var misses: Int,
) : RollingDefectTargetStore {
    override fun find(code: String): RollingDefectTarget? {
        val hit = (cache.get(code) as RollingDefectTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDefectTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDefectTarget> = inner.list()
}

fun newCachedRollingDefectTargetStore(inner: RollingDefectTargetStore, ttl: Int, clock: Clock): RollingDefectTargetStore {
    return CachedRollingDefectTargetStore(
        inner = inner,
        cache = newTtlCache<RollingDefectTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling defect targets. Empty fields match all. */
data class RollingDefectTargetQuery(
    var country: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDefectTarget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDefectTarget>): MutableList<RollingDefectTarget> {
        val matched = filter(items) { candidate: RollingDefectTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDefectTargets(store: RollingDefectTargetStore, query: RollingDefectTargetQuery): MutableList<RollingDefectTarget> {
    return query.apply(store.list())
}
