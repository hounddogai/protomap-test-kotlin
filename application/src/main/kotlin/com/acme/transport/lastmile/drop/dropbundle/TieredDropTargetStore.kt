package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered drop targets by code. */
interface TieredDropTargetStore {
    fun find(code: String): TieredDropTarget?
    fun save(item: TieredDropTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDropTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDropTargetStore(
    var items: MutableMap<String, TieredDropTarget>,
    var order: MutableList<String>,
) : TieredDropTargetStore {
    override fun find(code: String): TieredDropTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDropTarget) {
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

    override fun list(): MutableList<TieredDropTarget> {
        val out = mutableListOf<TieredDropTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDropTargetStore(): TieredDropTargetStore {
    return MemoryTieredDropTargetStore(mutableMapOf<String, TieredDropTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDropTargetStore(
    var inner: TieredDropTargetStore,
    var cache: TtlCache<TieredDropTarget>,
    var hits: Int,
    var misses: Int,
) : TieredDropTargetStore {
    override fun find(code: String): TieredDropTarget? {
        val hit = (cache.get(code) as TieredDropTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDropTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDropTarget> = inner.list()
}

fun newCachedTieredDropTargetStore(inner: TieredDropTargetStore, ttl: Int, clock: Clock): TieredDropTargetStore {
    return CachedTieredDropTargetStore(
        inner = inner,
        cache = newTtlCache<TieredDropTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered drop targets. Empty fields match all. */
data class TieredDropTargetQuery(
    var segment: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDropTarget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TieredDropTarget>): MutableList<TieredDropTarget> {
        val matched = filter(items) { candidate: TieredDropTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDropTargets(store: TieredDropTargetStore, query: TieredDropTargetQuery): MutableList<TieredDropTarget> {
    return query.apply(store.list())
}
