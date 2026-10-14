package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan items by code. */
interface LanePlanItemStore {
    fun find(code: String): LanePlanItem?
    fun save(item: LanePlanItem)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanItem>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanItemStore(
    var items: MutableMap<String, LanePlanItem>,
    var order: MutableList<String>,
) : LanePlanItemStore {
    override fun find(code: String): LanePlanItem? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanItem) {
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

    override fun list(): MutableList<LanePlanItem> {
        val out = mutableListOf<LanePlanItem>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanItemStore(): LanePlanItemStore {
    return MemoryLanePlanItemStore(mutableMapOf<String, LanePlanItem>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanItemStore(
    var inner: LanePlanItemStore,
    var cache: TtlCache<LanePlanItem>,
    var hits: Int,
    var misses: Int,
) : LanePlanItemStore {
    override fun find(code: String): LanePlanItem? {
        val hit = (cache.get(code) as LanePlanItem?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanItem) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanItem> = inner.list()
}

fun newCachedLanePlanItemStore(inner: LanePlanItemStore, ttl: Int, clock: Clock): LanePlanItemStore {
    return CachedLanePlanItemStore(
        inner = inner,
        cache = newTtlCache<LanePlanItem>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan items. Empty fields match all. */
data class LanePlanItemQuery(
    var segment: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanItem): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<LanePlanItem>): MutableList<LanePlanItem> {
        val matched = filter(items) { candidate: LanePlanItem -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanItems(store: LanePlanItemStore, query: LanePlanItemQuery): MutableList<LanePlanItem> {
    return query.apply(store.list())
}
