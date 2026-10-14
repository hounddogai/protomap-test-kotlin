package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation cycles by code. */
interface ViolationCycleStore {
    fun find(code: String): ViolationCycle?
    fun save(item: ViolationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationCycleStore(
    var items: MutableMap<String, ViolationCycle>,
    var order: MutableList<String>,
) : ViolationCycleStore {
    override fun find(code: String): ViolationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationCycle) {
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

    override fun list(): MutableList<ViolationCycle> {
        val out = mutableListOf<ViolationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationCycleStore(): ViolationCycleStore {
    return MemoryViolationCycleStore(mutableMapOf<String, ViolationCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationCycleStore(
    var inner: ViolationCycleStore,
    var cache: TtlCache<ViolationCycle>,
    var hits: Int,
    var misses: Int,
) : ViolationCycleStore {
    override fun find(code: String): ViolationCycle? {
        val hit = (cache.get(code) as ViolationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationCycle> = inner.list()
}

fun newCachedViolationCycleStore(inner: ViolationCycleStore, ttl: Int, clock: Clock): ViolationCycleStore {
    return CachedViolationCycleStore(
        inner = inner,
        cache = newTtlCache<ViolationCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation cycles. Empty fields match all. */
data class ViolationCycleQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ViolationCycle>): MutableList<ViolationCycle> {
        val matched = filter(items) { candidate: ViolationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationCycles(store: ViolationCycleStore, query: ViolationCycleQuery): MutableList<ViolationCycle> {
    return query.apply(store.list())
}
