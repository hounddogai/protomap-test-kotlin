package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global qualification slots by code. */
interface GlobalQualificationSlotStore {
    fun find(code: String): GlobalQualificationSlot?
    fun save(item: GlobalQualificationSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalQualificationSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalQualificationSlotStore(
    var items: MutableMap<String, GlobalQualificationSlot>,
    var order: MutableList<String>,
) : GlobalQualificationSlotStore {
    override fun find(code: String): GlobalQualificationSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalQualificationSlot) {
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

    override fun list(): MutableList<GlobalQualificationSlot> {
        val out = mutableListOf<GlobalQualificationSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalQualificationSlotStore(): GlobalQualificationSlotStore {
    return MemoryGlobalQualificationSlotStore(
        mutableMapOf<String, GlobalQualificationSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalQualificationSlotStore(
    var inner: GlobalQualificationSlotStore,
    var cache: TtlCache<GlobalQualificationSlot>,
    var hits: Int,
    var misses: Int,
) : GlobalQualificationSlotStore {
    override fun find(code: String): GlobalQualificationSlot? {
        val hit = (cache.get(code) as GlobalQualificationSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalQualificationSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalQualificationSlot> = inner.list()
}

fun newCachedGlobalQualificationSlotStore(inner: GlobalQualificationSlotStore, ttl: Int, clock: Clock): GlobalQualificationSlotStore {
    return CachedGlobalQualificationSlotStore(
        inner = inner,
        cache = newTtlCache<GlobalQualificationSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global qualification slots. Empty fields match all. */
data class GlobalQualificationSlotQuery(
    var lane: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalQualificationSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<GlobalQualificationSlot>): MutableList<GlobalQualificationSlot> {
        val matched = filter(items) { candidate: GlobalQualificationSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalQualificationSlots(store: GlobalQualificationSlotStore, query: GlobalQualificationSlotQuery): MutableList<GlobalQualificationSlot> {
    return query.apply(store.list())
}
