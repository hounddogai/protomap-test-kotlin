package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge overrides by code. */
interface EvChargeOverrideStore {
    fun find(code: String): EvChargeOverride?
    fun save(item: EvChargeOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeOverrideStore(
    var items: MutableMap<String, EvChargeOverride>,
    var order: MutableList<String>,
) : EvChargeOverrideStore {
    override fun find(code: String): EvChargeOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeOverride) {
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

    override fun list(): MutableList<EvChargeOverride> {
        val out = mutableListOf<EvChargeOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeOverrideStore(): EvChargeOverrideStore {
    return MemoryEvChargeOverrideStore(mutableMapOf<String, EvChargeOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeOverrideStore(
    var inner: EvChargeOverrideStore,
    var cache: TtlCache<EvChargeOverride>,
    var hits: Int,
    var misses: Int,
) : EvChargeOverrideStore {
    override fun find(code: String): EvChargeOverride? {
        val hit = (cache.get(code) as EvChargeOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeOverride> = inner.list()
}

fun newCachedEvChargeOverrideStore(inner: EvChargeOverrideStore, ttl: Int, clock: Clock): EvChargeOverrideStore {
    return CachedEvChargeOverrideStore(
        inner = inner,
        cache = newTtlCache<EvChargeOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge overrides. Empty fields match all. */
data class EvChargeOverrideQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeOverride): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeOverride>): MutableList<EvChargeOverride> {
        val matched = filter(items) { candidate: EvChargeOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeOverrides(store: EvChargeOverrideStore, query: EvChargeOverrideQuery): MutableList<EvChargeOverride> {
    return query.apply(store.list())
}
