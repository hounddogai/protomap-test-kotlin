package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation rollups by code. */
interface ViolationRollupStore {
    fun find(code: String): ViolationRollup?
    fun save(item: ViolationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationRollupStore(
    var items: MutableMap<String, ViolationRollup>,
    var order: MutableList<String>,
) : ViolationRollupStore {
    override fun find(code: String): ViolationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationRollup) {
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

    override fun list(): MutableList<ViolationRollup> {
        val out = mutableListOf<ViolationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationRollupStore(): ViolationRollupStore {
    return MemoryViolationRollupStore(mutableMapOf<String, ViolationRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationRollupStore(
    var inner: ViolationRollupStore,
    var cache: TtlCache<ViolationRollup>,
    var hits: Int,
    var misses: Int,
) : ViolationRollupStore {
    override fun find(code: String): ViolationRollup? {
        val hit = (cache.get(code) as ViolationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationRollup> = inner.list()
}

fun newCachedViolationRollupStore(inner: ViolationRollupStore, ttl: Int, clock: Clock): ViolationRollupStore {
    return CachedViolationRollupStore(
        inner = inner,
        cache = newTtlCache<ViolationRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation rollups. Empty fields match all. */
data class ViolationRollupQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationRollup>): MutableList<ViolationRollup> {
        val matched = filter(items) { candidate: ViolationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationRollups(store: ViolationRollupStore, query: ViolationRollupQuery): MutableList<ViolationRollup> {
    return query.apply(store.list())
}
