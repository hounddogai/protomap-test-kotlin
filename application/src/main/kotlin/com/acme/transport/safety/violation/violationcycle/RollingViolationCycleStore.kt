package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling violation cycles by code. */
interface RollingViolationCycleStore {
    fun find(code: String): RollingViolationCycle?
    fun save(item: RollingViolationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingViolationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingViolationCycleStore(
    var items: MutableMap<String, RollingViolationCycle>,
    var order: MutableList<String>,
) : RollingViolationCycleStore {
    override fun find(code: String): RollingViolationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingViolationCycle) {
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

    override fun list(): MutableList<RollingViolationCycle> {
        val out = mutableListOf<RollingViolationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingViolationCycleStore(): RollingViolationCycleStore {
    return MemoryRollingViolationCycleStore(
        mutableMapOf<String, RollingViolationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingViolationCycleStore(
    var inner: RollingViolationCycleStore,
    var cache: TtlCache<RollingViolationCycle>,
    var hits: Int,
    var misses: Int,
) : RollingViolationCycleStore {
    override fun find(code: String): RollingViolationCycle? {
        val hit = (cache.get(code) as RollingViolationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingViolationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingViolationCycle> = inner.list()
}

fun newCachedRollingViolationCycleStore(inner: RollingViolationCycleStore, ttl: Int, clock: Clock): RollingViolationCycleStore {
    return CachedRollingViolationCycleStore(
        inner = inner,
        cache = newTtlCache<RollingViolationCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling violation cycles. Empty fields match all. */
data class RollingViolationCycleQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingViolationCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingViolationCycle>): MutableList<RollingViolationCycle> {
        val matched = filter(items) { candidate: RollingViolationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingViolationCycles(store: RollingViolationCycleStore, query: RollingViolationCycleQuery): MutableList<RollingViolationCycle> {
    return query.apply(store.list())
}
