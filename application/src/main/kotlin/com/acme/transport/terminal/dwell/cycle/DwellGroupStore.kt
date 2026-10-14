package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell groups by code. */
interface DwellGroupStore {
    fun find(code: String): DwellGroup?
    fun save(item: DwellGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellGroupStore(
    var items: MutableMap<String, DwellGroup>,
    var order: MutableList<String>,
) : DwellGroupStore {
    override fun find(code: String): DwellGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellGroup) {
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

    override fun list(): MutableList<DwellGroup> {
        val out = mutableListOf<DwellGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellGroupStore(): DwellGroupStore {
    return MemoryDwellGroupStore(mutableMapOf<String, DwellGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellGroupStore(
    var inner: DwellGroupStore,
    var cache: TtlCache<DwellGroup>,
    var hits: Int,
    var misses: Int,
) : DwellGroupStore {
    override fun find(code: String): DwellGroup? {
        val hit = (cache.get(code) as DwellGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellGroup> = inner.list()
}

fun newCachedDwellGroupStore(inner: DwellGroupStore, ttl: Int, clock: Clock): DwellGroupStore {
    return CachedDwellGroupStore(
        inner = inner,
        cache = newTtlCache<DwellGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell groups. Empty fields match all. */
data class DwellGroupQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DwellGroup>): MutableList<DwellGroup> {
        val matched = filter(items) { candidate: DwellGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellGroups(store: DwellGroupStore, query: DwellGroupQuery): MutableList<DwellGroup> {
    return query.apply(store.list())
}
