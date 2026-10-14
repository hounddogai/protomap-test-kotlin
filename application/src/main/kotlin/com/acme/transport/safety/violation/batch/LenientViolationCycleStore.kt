package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient violation cycles by code. */
interface LenientViolationCycleStore {
    fun find(code: String): LenientViolationCycle?
    fun save(item: LenientViolationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientViolationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientViolationCycleStore(
    var items: MutableMap<String, LenientViolationCycle>,
    var order: MutableList<String>,
) : LenientViolationCycleStore {
    override fun find(code: String): LenientViolationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientViolationCycle) {
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

    override fun list(): MutableList<LenientViolationCycle> {
        val out = mutableListOf<LenientViolationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientViolationCycleStore(): LenientViolationCycleStore {
    return MemoryLenientViolationCycleStore(
        mutableMapOf<String, LenientViolationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientViolationCycleStore(
    var inner: LenientViolationCycleStore,
    var cache: TtlCache<LenientViolationCycle>,
    var hits: Int,
    var misses: Int,
) : LenientViolationCycleStore {
    override fun find(code: String): LenientViolationCycle? {
        val hit = (cache.get(code) as LenientViolationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientViolationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientViolationCycle> = inner.list()
}

fun newCachedLenientViolationCycleStore(inner: LenientViolationCycleStore, ttl: Int, clock: Clock): LenientViolationCycleStore {
    return CachedLenientViolationCycleStore(
        inner = inner,
        cache = newTtlCache<LenientViolationCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient violation cycles. Empty fields match all. */
data class LenientViolationCycleQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientViolationCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientViolationCycle>): MutableList<LenientViolationCycle> {
        val matched = filter(items) { candidate: LenientViolationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientViolationCycles(store: LenientViolationCycleStore, query: LenientViolationCycleQuery): MutableList<LenientViolationCycle> {
    return query.apply(store.list())
}
