package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door profiles by code. */
interface DockDoorProfileStore {
    fun find(code: String): DockDoorProfile?
    fun save(item: DockDoorProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorProfileStore(
    var items: MutableMap<String, DockDoorProfile>,
    var order: MutableList<String>,
) : DockDoorProfileStore {
    override fun find(code: String): DockDoorProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorProfile) {
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

    override fun list(): MutableList<DockDoorProfile> {
        val out = mutableListOf<DockDoorProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorProfileStore(): DockDoorProfileStore {
    return MemoryDockDoorProfileStore(mutableMapOf<String, DockDoorProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorProfileStore(
    var inner: DockDoorProfileStore,
    var cache: TtlCache<DockDoorProfile>,
    var hits: Int,
    var misses: Int,
) : DockDoorProfileStore {
    override fun find(code: String): DockDoorProfile? {
        val hit = (cache.get(code) as DockDoorProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorProfile> = inner.list()
}

fun newCachedDockDoorProfileStore(inner: DockDoorProfileStore, ttl: Int, clock: Clock): DockDoorProfileStore {
    return CachedDockDoorProfileStore(
        inner = inner,
        cache = newTtlCache<DockDoorProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door profiles. Empty fields match all. */
data class DockDoorProfileQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorProfile): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorProfile>): MutableList<DockDoorProfile> {
        val matched = filter(items) { candidate: DockDoorProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorProfiles(store: DockDoorProfileStore, query: DockDoorProfileQuery): MutableList<DockDoorProfile> {
    return query.apply(store.list())
}
