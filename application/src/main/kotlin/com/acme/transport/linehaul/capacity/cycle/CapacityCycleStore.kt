package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity cycles by code. */
interface CapacityCycleStore {
    fun find(code: String): CapacityCycle?
    fun save(item: CapacityCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityCycleStore(
    var items: MutableMap<String, CapacityCycle>,
    var order: MutableList<String>,
) : CapacityCycleStore {
    override fun find(code: String): CapacityCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityCycle) {
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

    override fun list(): MutableList<CapacityCycle> {
        val out = mutableListOf<CapacityCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityCycleStore(): CapacityCycleStore {
    return MemoryCapacityCycleStore(mutableMapOf<String, CapacityCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityCycleStore(
    var inner: CapacityCycleStore,
    var cache: TtlCache<CapacityCycle>,
    var hits: Int,
    var misses: Int,
) : CapacityCycleStore {
    override fun find(code: String): CapacityCycle? {
        val hit = (cache.get(code) as CapacityCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityCycle> = inner.list()
}

fun newCachedCapacityCycleStore(inner: CapacityCycleStore, ttl: Int, clock: Clock): CapacityCycleStore {
    return CachedCapacityCycleStore(
        inner = inner,
        cache = newTtlCache<CapacityCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity cycles. Empty fields match all. */
data class CapacityCycleQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityCycle): Boolean {
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

    fun apply(items: MutableList<CapacityCycle>): MutableList<CapacityCycle> {
        val matched = filter(items) { candidate: CapacityCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityCycles(store: CapacityCycleStore, query: CapacityCycleQuery): MutableList<CapacityCycle> {
    return query.apply(store.list())
}
