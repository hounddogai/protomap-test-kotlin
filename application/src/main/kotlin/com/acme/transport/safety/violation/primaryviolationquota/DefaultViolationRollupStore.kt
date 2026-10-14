package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default violation rollups by code. */
interface DefaultViolationRollupStore {
    fun find(code: String): DefaultViolationRollup?
    fun save(item: DefaultViolationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultViolationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultViolationRollupStore(
    var items: MutableMap<String, DefaultViolationRollup>,
    var order: MutableList<String>,
) : DefaultViolationRollupStore {
    override fun find(code: String): DefaultViolationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultViolationRollup) {
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

    override fun list(): MutableList<DefaultViolationRollup> {
        val out = mutableListOf<DefaultViolationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultViolationRollupStore(): DefaultViolationRollupStore {
    return MemoryDefaultViolationRollupStore(
        mutableMapOf<String, DefaultViolationRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultViolationRollupStore(
    var inner: DefaultViolationRollupStore,
    var cache: TtlCache<DefaultViolationRollup>,
    var hits: Int,
    var misses: Int,
) : DefaultViolationRollupStore {
    override fun find(code: String): DefaultViolationRollup? {
        val hit = (cache.get(code) as DefaultViolationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultViolationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultViolationRollup> = inner.list()
}

fun newCachedDefaultViolationRollupStore(inner: DefaultViolationRollupStore, ttl: Int, clock: Clock): DefaultViolationRollupStore {
    return CachedDefaultViolationRollupStore(
        inner = inner,
        cache = newTtlCache<DefaultViolationRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default violation rollups. Empty fields match all. */
data class DefaultViolationRollupQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultViolationRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultViolationRollup>): MutableList<DefaultViolationRollup> {
        val matched = filter(items) { candidate: DefaultViolationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultViolationRollups(store: DefaultViolationRollupStore, query: DefaultViolationRollupQuery): MutableList<DefaultViolationRollup> {
    return query.apply(store.list())
}
