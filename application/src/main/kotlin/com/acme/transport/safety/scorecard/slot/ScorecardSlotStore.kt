package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard slots by code. */
interface ScorecardSlotStore {
    fun find(code: String): ScorecardSlot?
    fun save(item: ScorecardSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardSlotStore(
    var items: MutableMap<String, ScorecardSlot>,
    var order: MutableList<String>,
) : ScorecardSlotStore {
    override fun find(code: String): ScorecardSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardSlot) {
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

    override fun list(): MutableList<ScorecardSlot> {
        val out = mutableListOf<ScorecardSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardSlotStore(): ScorecardSlotStore {
    return MemoryScorecardSlotStore(mutableMapOf<String, ScorecardSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardSlotStore(
    var inner: ScorecardSlotStore,
    var cache: TtlCache<ScorecardSlot>,
    var hits: Int,
    var misses: Int,
) : ScorecardSlotStore {
    override fun find(code: String): ScorecardSlot? {
        val hit = (cache.get(code) as ScorecardSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardSlot> = inner.list()
}

fun newCachedScorecardSlotStore(inner: ScorecardSlotStore, ttl: Int, clock: Clock): ScorecardSlotStore {
    return CachedScorecardSlotStore(
        inner = inner,
        cache = newTtlCache<ScorecardSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard slots. Empty fields match all. */
data class ScorecardSlotQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardSlot): Boolean {
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

    fun apply(items: MutableList<ScorecardSlot>): MutableList<ScorecardSlot> {
        val matched = filter(items) { candidate: ScorecardSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardSlots(store: ScorecardSlotStore, query: ScorecardSlotQuery): MutableList<ScorecardSlot> {
    return query.apply(store.list())
}
