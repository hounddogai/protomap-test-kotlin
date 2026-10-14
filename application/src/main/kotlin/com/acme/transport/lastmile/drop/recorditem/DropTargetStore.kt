package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop targets by code. */
interface DropTargetStore {
    fun find(code: String): DropTarget?
    fun save(item: DropTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropTargetStore(
    var items: MutableMap<String, DropTarget>,
    var order: MutableList<String>,
) : DropTargetStore {
    override fun find(code: String): DropTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropTarget) {
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

    override fun list(): MutableList<DropTarget> {
        val out = mutableListOf<DropTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropTargetStore(): DropTargetStore {
    return MemoryDropTargetStore(mutableMapOf<String, DropTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropTargetStore(
    var inner: DropTargetStore,
    var cache: TtlCache<DropTarget>,
    var hits: Int,
    var misses: Int,
) : DropTargetStore {
    override fun find(code: String): DropTarget? {
        val hit = (cache.get(code) as DropTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropTarget> = inner.list()
}

fun newCachedDropTargetStore(inner: DropTargetStore, ttl: Int, clock: Clock): DropTargetStore {
    return CachedDropTargetStore(
        inner = inner,
        cache = newTtlCache<DropTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop targets. Empty fields match all. */
data class DropTargetQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropTarget>): MutableList<DropTarget> {
        val matched = filter(items) { candidate: DropTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropTargets(store: DropTargetStore, query: DropTargetQuery): MutableList<DropTarget> {
    return query.apply(store.list())
}
