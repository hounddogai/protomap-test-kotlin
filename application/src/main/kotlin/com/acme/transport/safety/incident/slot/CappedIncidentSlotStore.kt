package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped incident slots by code. */
interface CappedIncidentSlotStore {
    fun find(code: String): CappedIncidentSlot?
    fun save(item: CappedIncidentSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedIncidentSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedIncidentSlotStore(
    var items: MutableMap<String, CappedIncidentSlot>,
    var order: MutableList<String>,
) : CappedIncidentSlotStore {
    override fun find(code: String): CappedIncidentSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedIncidentSlot) {
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

    override fun list(): MutableList<CappedIncidentSlot> {
        val out = mutableListOf<CappedIncidentSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedIncidentSlotStore(): CappedIncidentSlotStore {
    return MemoryCappedIncidentSlotStore(mutableMapOf<String, CappedIncidentSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedIncidentSlotStore(
    var inner: CappedIncidentSlotStore,
    var cache: TtlCache<CappedIncidentSlot>,
    var hits: Int,
    var misses: Int,
) : CappedIncidentSlotStore {
    override fun find(code: String): CappedIncidentSlot? {
        val hit = (cache.get(code) as CappedIncidentSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedIncidentSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedIncidentSlot> = inner.list()
}

fun newCachedCappedIncidentSlotStore(inner: CappedIncidentSlotStore, ttl: Int, clock: Clock): CappedIncidentSlotStore {
    return CachedCappedIncidentSlotStore(
        inner = inner,
        cache = newTtlCache<CappedIncidentSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped incident slots. Empty fields match all. */
data class CappedIncidentSlotQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedIncidentSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedIncidentSlot>): MutableList<CappedIncidentSlot> {
        val matched = filter(items) { candidate: CappedIncidentSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedIncidentSlots(store: CappedIncidentSlotStore, query: CappedIncidentSlotQuery): MutableList<CappedIncidentSlot> {
    return query.apply(store.list())
}
