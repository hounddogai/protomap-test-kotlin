package com.acme.transport.fleetops.defect.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect targets by code. */
interface DefectTargetStore {
    fun find(code: String): DefectTarget?
    fun save(item: DefectTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectTargetStore(
    var items: MutableMap<String, DefectTarget>,
    var order: MutableList<String>,
) : DefectTargetStore {
    override fun find(code: String): DefectTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectTarget) {
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

    override fun list(): MutableList<DefectTarget> {
        val out = mutableListOf<DefectTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectTargetStore(): DefectTargetStore {
    return MemoryDefectTargetStore(mutableMapOf<String, DefectTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectTargetStore(
    var inner: DefectTargetStore,
    var cache: TtlCache<DefectTarget>,
    var hits: Int,
    var misses: Int,
) : DefectTargetStore {
    override fun find(code: String): DefectTarget? {
        val hit = (cache.get(code) as DefectTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectTarget> = inner.list()
}

fun newCachedDefectTargetStore(inner: DefectTargetStore, ttl: Int, clock: Clock): DefectTargetStore {
    return CachedDefectTargetStore(
        inner = inner,
        cache = newTtlCache<DefectTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect targets. Empty fields match all. */
data class DefectTargetQuery(
    var locale: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectTarget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectTarget>): MutableList<DefectTarget> {
        val matched = filter(items) { candidate: DefectTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectTargets(store: DefectTargetStore, query: DefectTargetQuery): MutableList<DefectTarget> {
    return query.apply(store.list())
}
