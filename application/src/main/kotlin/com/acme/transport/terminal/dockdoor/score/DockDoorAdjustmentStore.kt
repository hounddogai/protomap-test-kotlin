package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door adjustments by code. */
interface DockDoorAdjustmentStore {
    fun find(code: String): DockDoorAdjustment?
    fun save(item: DockDoorAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorAdjustmentStore(
    var items: MutableMap<String, DockDoorAdjustment>,
    var order: MutableList<String>,
) : DockDoorAdjustmentStore {
    override fun find(code: String): DockDoorAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorAdjustment) {
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

    override fun list(): MutableList<DockDoorAdjustment> {
        val out = mutableListOf<DockDoorAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorAdjustmentStore(): DockDoorAdjustmentStore {
    return MemoryDockDoorAdjustmentStore(mutableMapOf<String, DockDoorAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorAdjustmentStore(
    var inner: DockDoorAdjustmentStore,
    var cache: TtlCache<DockDoorAdjustment>,
    var hits: Int,
    var misses: Int,
) : DockDoorAdjustmentStore {
    override fun find(code: String): DockDoorAdjustment? {
        val hit = (cache.get(code) as DockDoorAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorAdjustment> = inner.list()
}

fun newCachedDockDoorAdjustmentStore(inner: DockDoorAdjustmentStore, ttl: Int, clock: Clock): DockDoorAdjustmentStore {
    return CachedDockDoorAdjustmentStore(
        inner = inner,
        cache = newTtlCache<DockDoorAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door adjustments. Empty fields match all. */
data class DockDoorAdjustmentQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorAdjustment>): MutableList<DockDoorAdjustment> {
        val matched = filter(items) { candidate: DockDoorAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorAdjustments(store: DockDoorAdjustmentStore, query: DockDoorAdjustmentQuery): MutableList<DockDoorAdjustment> {
    return query.apply(store.list())
}
