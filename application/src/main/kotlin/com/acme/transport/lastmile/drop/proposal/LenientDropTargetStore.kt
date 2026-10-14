package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient drop targets by code. */
interface LenientDropTargetStore {
    fun find(code: String): LenientDropTarget?
    fun save(item: LenientDropTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDropTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDropTargetStore(
    var items: MutableMap<String, LenientDropTarget>,
    var order: MutableList<String>,
) : LenientDropTargetStore {
    override fun find(code: String): LenientDropTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDropTarget) {
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

    override fun list(): MutableList<LenientDropTarget> {
        val out = mutableListOf<LenientDropTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDropTargetStore(): LenientDropTargetStore {
    return MemoryLenientDropTargetStore(mutableMapOf<String, LenientDropTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDropTargetStore(
    var inner: LenientDropTargetStore,
    var cache: TtlCache<LenientDropTarget>,
    var hits: Int,
    var misses: Int,
) : LenientDropTargetStore {
    override fun find(code: String): LenientDropTarget? {
        val hit = (cache.get(code) as LenientDropTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDropTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDropTarget> = inner.list()
}

fun newCachedLenientDropTargetStore(inner: LenientDropTargetStore, ttl: Int, clock: Clock): LenientDropTargetStore {
    return CachedLenientDropTargetStore(
        inner = inner,
        cache = newTtlCache<LenientDropTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient drop targets. Empty fields match all. */
data class LenientDropTargetQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDropTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDropTarget>): MutableList<LenientDropTarget> {
        val matched = filter(items) { candidate: LenientDropTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDropTargets(store: LenientDropTargetStore, query: LenientDropTargetQuery): MutableList<LenientDropTarget> {
    return query.apply(store.list())
}
