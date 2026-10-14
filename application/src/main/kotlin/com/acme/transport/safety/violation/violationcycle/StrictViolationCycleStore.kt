package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict violation cycles by code. */
interface StrictViolationCycleStore {
    fun find(code: String): StrictViolationCycle?
    fun save(item: StrictViolationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictViolationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictViolationCycleStore(
    var items: MutableMap<String, StrictViolationCycle>,
    var order: MutableList<String>,
) : StrictViolationCycleStore {
    override fun find(code: String): StrictViolationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictViolationCycle) {
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

    override fun list(): MutableList<StrictViolationCycle> {
        val out = mutableListOf<StrictViolationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictViolationCycleStore(): StrictViolationCycleStore {
    return MemoryStrictViolationCycleStore(
        mutableMapOf<String, StrictViolationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictViolationCycleStore(
    var inner: StrictViolationCycleStore,
    var cache: TtlCache<StrictViolationCycle>,
    var hits: Int,
    var misses: Int,
) : StrictViolationCycleStore {
    override fun find(code: String): StrictViolationCycle? {
        val hit = (cache.get(code) as StrictViolationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictViolationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictViolationCycle> = inner.list()
}

fun newCachedStrictViolationCycleStore(inner: StrictViolationCycleStore, ttl: Int, clock: Clock): StrictViolationCycleStore {
    return CachedStrictViolationCycleStore(
        inner = inner,
        cache = newTtlCache<StrictViolationCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict violation cycles. Empty fields match all. */
data class StrictViolationCycleQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictViolationCycle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictViolationCycle>): MutableList<StrictViolationCycle> {
        val matched = filter(items) { candidate: StrictViolationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictViolationCycles(store: StrictViolationCycleStore, query: StrictViolationCycleQuery): MutableList<StrictViolationCycle> {
    return query.apply(store.list())
}
