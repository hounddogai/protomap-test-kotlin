package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional drop targets by code. */
interface RegionalDropTargetStore {
    fun find(code: String): RegionalDropTarget?
    fun save(item: RegionalDropTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDropTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDropTargetStore(
    var items: MutableMap<String, RegionalDropTarget>,
    var order: MutableList<String>,
) : RegionalDropTargetStore {
    override fun find(code: String): RegionalDropTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDropTarget) {
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

    override fun list(): MutableList<RegionalDropTarget> {
        val out = mutableListOf<RegionalDropTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDropTargetStore(): RegionalDropTargetStore {
    return MemoryRegionalDropTargetStore(mutableMapOf<String, RegionalDropTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDropTargetStore(
    var inner: RegionalDropTargetStore,
    var cache: TtlCache<RegionalDropTarget>,
    var hits: Int,
    var misses: Int,
) : RegionalDropTargetStore {
    override fun find(code: String): RegionalDropTarget? {
        val hit = (cache.get(code) as RegionalDropTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDropTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDropTarget> = inner.list()
}

fun newCachedRegionalDropTargetStore(inner: RegionalDropTargetStore, ttl: Int, clock: Clock): RegionalDropTargetStore {
    return CachedRegionalDropTargetStore(
        inner = inner,
        cache = newTtlCache<RegionalDropTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional drop targets. Empty fields match all. */
data class RegionalDropTargetQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDropTarget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RegionalDropTarget>): MutableList<RegionalDropTarget> {
        val matched = filter(items) { candidate: RegionalDropTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDropTargets(store: RegionalDropTargetStore, query: RegionalDropTargetQuery): MutableList<RegionalDropTarget> {
    return query.apply(store.list())
}
