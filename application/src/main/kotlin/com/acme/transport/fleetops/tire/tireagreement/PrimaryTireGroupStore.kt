package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary tire groups by code. */
interface PrimaryTireGroupStore {
    fun find(code: String): PrimaryTireGroup?
    fun save(item: PrimaryTireGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTireGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTireGroupStore(
    var items: MutableMap<String, PrimaryTireGroup>,
    var order: MutableList<String>,
) : PrimaryTireGroupStore {
    override fun find(code: String): PrimaryTireGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTireGroup) {
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

    override fun list(): MutableList<PrimaryTireGroup> {
        val out = mutableListOf<PrimaryTireGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTireGroupStore(): PrimaryTireGroupStore {
    return MemoryPrimaryTireGroupStore(mutableMapOf<String, PrimaryTireGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTireGroupStore(
    var inner: PrimaryTireGroupStore,
    var cache: TtlCache<PrimaryTireGroup>,
    var hits: Int,
    var misses: Int,
) : PrimaryTireGroupStore {
    override fun find(code: String): PrimaryTireGroup? {
        val hit = (cache.get(code) as PrimaryTireGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTireGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTireGroup> = inner.list()
}

fun newCachedPrimaryTireGroupStore(inner: PrimaryTireGroupStore, ttl: Int, clock: Clock): PrimaryTireGroupStore {
    return CachedPrimaryTireGroupStore(
        inner = inner,
        cache = newTtlCache<PrimaryTireGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tire groups. Empty fields match all. */
data class PrimaryTireGroupQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTireGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTireGroup>): MutableList<PrimaryTireGroup> {
        val matched = filter(items) { candidate: PrimaryTireGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTireGroups(store: PrimaryTireGroupStore, query: PrimaryTireGroupQuery): MutableList<PrimaryTireGroup> {
    return query.apply(store.list())
}
