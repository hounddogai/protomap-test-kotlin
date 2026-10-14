package com.acme.transport.network.zone.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped zone groups by code. */
interface CappedZoneGroupStore {
    fun find(code: String): CappedZoneGroup?
    fun save(item: CappedZoneGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedZoneGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedZoneGroupStore(
    var items: MutableMap<String, CappedZoneGroup>,
    var order: MutableList<String>,
) : CappedZoneGroupStore {
    override fun find(code: String): CappedZoneGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedZoneGroup) {
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

    override fun list(): MutableList<CappedZoneGroup> {
        val out = mutableListOf<CappedZoneGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedZoneGroupStore(): CappedZoneGroupStore {
    return MemoryCappedZoneGroupStore(mutableMapOf<String, CappedZoneGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedZoneGroupStore(
    var inner: CappedZoneGroupStore,
    var cache: TtlCache<CappedZoneGroup>,
    var hits: Int,
    var misses: Int,
) : CappedZoneGroupStore {
    override fun find(code: String): CappedZoneGroup? {
        val hit = (cache.get(code) as CappedZoneGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedZoneGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedZoneGroup> = inner.list()
}

fun newCachedCappedZoneGroupStore(inner: CappedZoneGroupStore, ttl: Int, clock: Clock): CappedZoneGroupStore {
    return CachedCappedZoneGroupStore(
        inner = inner,
        cache = newTtlCache<CappedZoneGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped zone groups. Empty fields match all. */
data class CappedZoneGroupQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedZoneGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedZoneGroup>): MutableList<CappedZoneGroup> {
        val matched = filter(items) { candidate: CappedZoneGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedZoneGroups(store: CappedZoneGroupStore, query: CappedZoneGroupQuery): MutableList<CappedZoneGroup> {
    return query.apply(store.list())
}
