package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door groups by code. */
interface DockDoorGroupStore {
    fun find(code: String): DockDoorGroup?
    fun save(item: DockDoorGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorGroupStore(
    var items: MutableMap<String, DockDoorGroup>,
    var order: MutableList<String>,
) : DockDoorGroupStore {
    override fun find(code: String): DockDoorGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorGroup) {
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

    override fun list(): MutableList<DockDoorGroup> {
        val out = mutableListOf<DockDoorGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorGroupStore(): DockDoorGroupStore {
    return MemoryDockDoorGroupStore(mutableMapOf<String, DockDoorGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorGroupStore(
    var inner: DockDoorGroupStore,
    var cache: TtlCache<DockDoorGroup>,
    var hits: Int,
    var misses: Int,
) : DockDoorGroupStore {
    override fun find(code: String): DockDoorGroup? {
        val hit = (cache.get(code) as DockDoorGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorGroup> = inner.list()
}

fun newCachedDockDoorGroupStore(inner: DockDoorGroupStore, ttl: Int, clock: Clock): DockDoorGroupStore {
    return CachedDockDoorGroupStore(
        inner = inner,
        cache = newTtlCache<DockDoorGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door groups. Empty fields match all. */
data class DockDoorGroupQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorGroup>): MutableList<DockDoorGroup> {
        val matched = filter(items) { candidate: DockDoorGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorGroups(store: DockDoorGroupStore, query: DockDoorGroupQuery): MutableList<DockDoorGroup> {
    return query.apply(store.list())
}
