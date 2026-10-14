package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending violation rollups by code. */
interface PendingViolationRollupStore {
    fun find(code: String): PendingViolationRollup?
    fun save(item: PendingViolationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingViolationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingViolationRollupStore(
    var items: MutableMap<String, PendingViolationRollup>,
    var order: MutableList<String>,
) : PendingViolationRollupStore {
    override fun find(code: String): PendingViolationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingViolationRollup) {
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

    override fun list(): MutableList<PendingViolationRollup> {
        val out = mutableListOf<PendingViolationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingViolationRollupStore(): PendingViolationRollupStore {
    return MemoryPendingViolationRollupStore(
        mutableMapOf<String, PendingViolationRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingViolationRollupStore(
    var inner: PendingViolationRollupStore,
    var cache: TtlCache<PendingViolationRollup>,
    var hits: Int,
    var misses: Int,
) : PendingViolationRollupStore {
    override fun find(code: String): PendingViolationRollup? {
        val hit = (cache.get(code) as PendingViolationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingViolationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingViolationRollup> = inner.list()
}

fun newCachedPendingViolationRollupStore(inner: PendingViolationRollupStore, ttl: Int, clock: Clock): PendingViolationRollupStore {
    return CachedPendingViolationRollupStore(
        inner = inner,
        cache = newTtlCache<PendingViolationRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending violation rollups. Empty fields match all. */
data class PendingViolationRollupQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingViolationRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingViolationRollup>): MutableList<PendingViolationRollup> {
        val matched = filter(items) { candidate: PendingViolationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingViolationRollups(store: PendingViolationRollupStore, query: PendingViolationRollupQuery): MutableList<PendingViolationRollup> {
    return query.apply(store.list())
}
