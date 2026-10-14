package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback violation slots by code. */
interface FallbackViolationSlotStore {
    fun find(code: String): FallbackViolationSlot?
    fun save(item: FallbackViolationSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackViolationSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackViolationSlotStore(
    var items: MutableMap<String, FallbackViolationSlot>,
    var order: MutableList<String>,
) : FallbackViolationSlotStore {
    override fun find(code: String): FallbackViolationSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackViolationSlot) {
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

    override fun list(): MutableList<FallbackViolationSlot> {
        val out = mutableListOf<FallbackViolationSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackViolationSlotStore(): FallbackViolationSlotStore {
    return MemoryFallbackViolationSlotStore(
        mutableMapOf<String, FallbackViolationSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackViolationSlotStore(
    var inner: FallbackViolationSlotStore,
    var cache: TtlCache<FallbackViolationSlot>,
    var hits: Int,
    var misses: Int,
) : FallbackViolationSlotStore {
    override fun find(code: String): FallbackViolationSlot? {
        val hit = (cache.get(code) as FallbackViolationSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackViolationSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackViolationSlot> = inner.list()
}

fun newCachedFallbackViolationSlotStore(inner: FallbackViolationSlotStore, ttl: Int, clock: Clock): FallbackViolationSlotStore {
    return CachedFallbackViolationSlotStore(
        inner = inner,
        cache = newTtlCache<FallbackViolationSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback violation slots. Empty fields match all. */
data class FallbackViolationSlotQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackViolationSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackViolationSlot>): MutableList<FallbackViolationSlot> {
        val matched = filter(items) { candidate: FallbackViolationSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackViolationSlots(store: FallbackViolationSlotStore, query: FallbackViolationSlotQuery): MutableList<FallbackViolationSlot> {
    return query.apply(store.list())
}
