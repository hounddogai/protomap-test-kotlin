package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending lane plan items by code. */
interface PendingLanePlanItemStore {
    fun find(code: String): PendingLanePlanItem?
    fun save(item: PendingLanePlanItem)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLanePlanItem>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLanePlanItemStore(
    var items: MutableMap<String, PendingLanePlanItem>,
    var order: MutableList<String>,
) : PendingLanePlanItemStore {
    override fun find(code: String): PendingLanePlanItem? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLanePlanItem) {
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

    override fun list(): MutableList<PendingLanePlanItem> {
        val out = mutableListOf<PendingLanePlanItem>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLanePlanItemStore(): PendingLanePlanItemStore {
    return MemoryPendingLanePlanItemStore(mutableMapOf<String, PendingLanePlanItem>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLanePlanItemStore(
    var inner: PendingLanePlanItemStore,
    var cache: TtlCache<PendingLanePlanItem>,
    var hits: Int,
    var misses: Int,
) : PendingLanePlanItemStore {
    override fun find(code: String): PendingLanePlanItem? {
        val hit = (cache.get(code) as PendingLanePlanItem?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLanePlanItem) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLanePlanItem> = inner.list()
}

fun newCachedPendingLanePlanItemStore(inner: PendingLanePlanItemStore, ttl: Int, clock: Clock): PendingLanePlanItemStore {
    return CachedPendingLanePlanItemStore(
        inner = inner,
        cache = newTtlCache<PendingLanePlanItem>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending lane plan items. Empty fields match all. */
data class PendingLanePlanItemQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLanePlanItem): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLanePlanItem>): MutableList<PendingLanePlanItem> {
        val matched = filter(items) { candidate: PendingLanePlanItem -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLanePlanItems(store: PendingLanePlanItemStore, query: PendingLanePlanItemQuery): MutableList<PendingLanePlanItem> {
    return query.apply(store.list())
}
