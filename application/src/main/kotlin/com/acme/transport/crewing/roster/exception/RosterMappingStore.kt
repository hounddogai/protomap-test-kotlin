package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster mappings by code. */
interface RosterMappingStore {
    fun find(code: String): RosterMapping?
    fun save(item: RosterMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterMappingStore(
    var items: MutableMap<String, RosterMapping>,
    var order: MutableList<String>,
) : RosterMappingStore {
    override fun find(code: String): RosterMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterMapping) {
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

    override fun list(): MutableList<RosterMapping> {
        val out = mutableListOf<RosterMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterMappingStore(): RosterMappingStore {
    return MemoryRosterMappingStore(mutableMapOf<String, RosterMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterMappingStore(
    var inner: RosterMappingStore,
    var cache: TtlCache<RosterMapping>,
    var hits: Int,
    var misses: Int,
) : RosterMappingStore {
    override fun find(code: String): RosterMapping? {
        val hit = (cache.get(code) as RosterMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterMapping> = inner.list()
}

fun newCachedRosterMappingStore(inner: RosterMappingStore, ttl: Int, clock: Clock): RosterMappingStore {
    return CachedRosterMappingStore(
        inner = inner,
        cache = newTtlCache<RosterMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster mappings. Empty fields match all. */
data class RosterMappingQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterMapping>): MutableList<RosterMapping> {
        val matched = filter(items) { candidate: RosterMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterMappings(store: RosterMappingStore, query: RosterMappingQuery): MutableList<RosterMapping> {
    return query.apply(store.list())
}
