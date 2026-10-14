package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled attempt slots by code. */
interface ScheduledAttemptSlotStore {
    fun find(code: String): ScheduledAttemptSlot?
    fun save(item: ScheduledAttemptSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAttemptSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAttemptSlotStore(
    var items: MutableMap<String, ScheduledAttemptSlot>,
    var order: MutableList<String>,
) : ScheduledAttemptSlotStore {
    override fun find(code: String): ScheduledAttemptSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAttemptSlot) {
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

    override fun list(): MutableList<ScheduledAttemptSlot> {
        val out = mutableListOf<ScheduledAttemptSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAttemptSlotStore(): ScheduledAttemptSlotStore {
    return MemoryScheduledAttemptSlotStore(
        mutableMapOf<String, ScheduledAttemptSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAttemptSlotStore(
    var inner: ScheduledAttemptSlotStore,
    var cache: TtlCache<ScheduledAttemptSlot>,
    var hits: Int,
    var misses: Int,
) : ScheduledAttemptSlotStore {
    override fun find(code: String): ScheduledAttemptSlot? {
        val hit = (cache.get(code) as ScheduledAttemptSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAttemptSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAttemptSlot> = inner.list()
}

fun newCachedScheduledAttemptSlotStore(inner: ScheduledAttemptSlotStore, ttl: Int, clock: Clock): ScheduledAttemptSlotStore {
    return CachedScheduledAttemptSlotStore(
        inner = inner,
        cache = newTtlCache<ScheduledAttemptSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled attempt slots. Empty fields match all. */
data class ScheduledAttemptSlotQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAttemptSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAttemptSlot>): MutableList<ScheduledAttemptSlot> {
        val matched = filter(items) { candidate: ScheduledAttemptSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAttemptSlots(store: ScheduledAttemptSlotStore, query: ScheduledAttemptSlotQuery): MutableList<ScheduledAttemptSlot> {
    return query.apply(store.list())
}
