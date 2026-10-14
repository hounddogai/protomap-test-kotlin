package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time groups by code. */
interface TransitTimeGroupStore {
    fun find(code: String): TransitTimeGroup?
    fun save(item: TransitTimeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeGroupStore(
    var items: MutableMap<String, TransitTimeGroup>,
    var order: MutableList<String>,
) : TransitTimeGroupStore {
    override fun find(code: String): TransitTimeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeGroup) {
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

    override fun list(): MutableList<TransitTimeGroup> {
        val out = mutableListOf<TransitTimeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeGroupStore(): TransitTimeGroupStore {
    return MemoryTransitTimeGroupStore(mutableMapOf<String, TransitTimeGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeGroupStore(
    var inner: TransitTimeGroupStore,
    var cache: TtlCache<TransitTimeGroup>,
    var hits: Int,
    var misses: Int,
) : TransitTimeGroupStore {
    override fun find(code: String): TransitTimeGroup? {
        val hit = (cache.get(code) as TransitTimeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeGroup> = inner.list()
}

fun newCachedTransitTimeGroupStore(inner: TransitTimeGroupStore, ttl: Int, clock: Clock): TransitTimeGroupStore {
    return CachedTransitTimeGroupStore(
        inner = inner,
        cache = newTtlCache<TransitTimeGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time groups. Empty fields match all. */
data class TransitTimeGroupQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TransitTimeGroup>): MutableList<TransitTimeGroup> {
        val matched = filter(items) { candidate: TransitTimeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeGroups(store: TransitTimeGroupStore, query: TransitTimeGroupQuery): MutableList<TransitTimeGroup> {
    return query.apply(store.list())
}
