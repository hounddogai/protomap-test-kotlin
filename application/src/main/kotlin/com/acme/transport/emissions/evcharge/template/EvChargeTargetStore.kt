package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge targets by code. */
interface EvChargeTargetStore {
    fun find(code: String): EvChargeTarget?
    fun save(item: EvChargeTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeTargetStore(
    var items: MutableMap<String, EvChargeTarget>,
    var order: MutableList<String>,
) : EvChargeTargetStore {
    override fun find(code: String): EvChargeTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeTarget) {
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

    override fun list(): MutableList<EvChargeTarget> {
        val out = mutableListOf<EvChargeTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeTargetStore(): EvChargeTargetStore {
    return MemoryEvChargeTargetStore(mutableMapOf<String, EvChargeTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeTargetStore(
    var inner: EvChargeTargetStore,
    var cache: TtlCache<EvChargeTarget>,
    var hits: Int,
    var misses: Int,
) : EvChargeTargetStore {
    override fun find(code: String): EvChargeTarget? {
        val hit = (cache.get(code) as EvChargeTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeTarget> = inner.list()
}

fun newCachedEvChargeTargetStore(inner: EvChargeTargetStore, ttl: Int, clock: Clock): EvChargeTargetStore {
    return CachedEvChargeTargetStore(
        inner = inner,
        cache = newTtlCache<EvChargeTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge targets. Empty fields match all. */
data class EvChargeTargetQuery(
    var region: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeTarget>): MutableList<EvChargeTarget> {
        val matched = filter(items) { candidate: EvChargeTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeTargets(store: EvChargeTargetStore, query: EvChargeTargetQuery): MutableList<EvChargeTarget> {
    return query.apply(store.list())
}
