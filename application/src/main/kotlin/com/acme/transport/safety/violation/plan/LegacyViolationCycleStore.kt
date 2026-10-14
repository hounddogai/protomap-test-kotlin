package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy violation cycles by code. */
interface LegacyViolationCycleStore {
    fun find(code: String): LegacyViolationCycle?
    fun save(item: LegacyViolationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyViolationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyViolationCycleStore(
    var items: MutableMap<String, LegacyViolationCycle>,
    var order: MutableList<String>,
) : LegacyViolationCycleStore {
    override fun find(code: String): LegacyViolationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyViolationCycle) {
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

    override fun list(): MutableList<LegacyViolationCycle> {
        val out = mutableListOf<LegacyViolationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyViolationCycleStore(): LegacyViolationCycleStore {
    return MemoryLegacyViolationCycleStore(
        mutableMapOf<String, LegacyViolationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyViolationCycleStore(
    var inner: LegacyViolationCycleStore,
    var cache: TtlCache<LegacyViolationCycle>,
    var hits: Int,
    var misses: Int,
) : LegacyViolationCycleStore {
    override fun find(code: String): LegacyViolationCycle? {
        val hit = (cache.get(code) as LegacyViolationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyViolationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyViolationCycle> = inner.list()
}

fun newCachedLegacyViolationCycleStore(inner: LegacyViolationCycleStore, ttl: Int, clock: Clock): LegacyViolationCycleStore {
    return CachedLegacyViolationCycleStore(
        inner = inner,
        cache = newTtlCache<LegacyViolationCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy violation cycles. Empty fields match all. */
data class LegacyViolationCycleQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyViolationCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyViolationCycle>): MutableList<LegacyViolationCycle> {
        val matched = filter(items) { candidate: LegacyViolationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyViolationCycles(store: LegacyViolationCycleStore, query: LegacyViolationCycleQuery): MutableList<LegacyViolationCycle> {
    return query.apply(store.list())
}
