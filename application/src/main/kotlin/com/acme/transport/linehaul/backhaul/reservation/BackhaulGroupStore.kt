package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul groups by code. */
interface BackhaulGroupStore {
    fun find(code: String): BackhaulGroup?
    fun save(item: BackhaulGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulGroupStore(
    var items: MutableMap<String, BackhaulGroup>,
    var order: MutableList<String>,
) : BackhaulGroupStore {
    override fun find(code: String): BackhaulGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulGroup) {
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

    override fun list(): MutableList<BackhaulGroup> {
        val out = mutableListOf<BackhaulGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulGroupStore(): BackhaulGroupStore {
    return MemoryBackhaulGroupStore(mutableMapOf<String, BackhaulGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulGroupStore(
    var inner: BackhaulGroupStore,
    var cache: TtlCache<BackhaulGroup>,
    var hits: Int,
    var misses: Int,
) : BackhaulGroupStore {
    override fun find(code: String): BackhaulGroup? {
        val hit = (cache.get(code) as BackhaulGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulGroup> = inner.list()
}

fun newCachedBackhaulGroupStore(inner: BackhaulGroupStore, ttl: Int, clock: Clock): BackhaulGroupStore {
    return CachedBackhaulGroupStore(
        inner = inner,
        cache = newTtlCache<BackhaulGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul groups. Empty fields match all. */
data class BackhaulGroupQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulGroup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<BackhaulGroup>): MutableList<BackhaulGroup> {
        val matched = filter(items) { candidate: BackhaulGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulGroups(store: BackhaulGroupStore, query: BackhaulGroupQuery): MutableList<BackhaulGroup> {
    return query.apply(store.list())
}
