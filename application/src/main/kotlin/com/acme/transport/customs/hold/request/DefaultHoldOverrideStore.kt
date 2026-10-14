package com.acme.transport.customs.hold.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default hold overrides by code. */
interface DefaultHoldOverrideStore {
    fun find(code: String): DefaultHoldOverride?
    fun save(item: DefaultHoldOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultHoldOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultHoldOverrideStore(
    var items: MutableMap<String, DefaultHoldOverride>,
    var order: MutableList<String>,
) : DefaultHoldOverrideStore {
    override fun find(code: String): DefaultHoldOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultHoldOverride) {
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

    override fun list(): MutableList<DefaultHoldOverride> {
        val out = mutableListOf<DefaultHoldOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultHoldOverrideStore(): DefaultHoldOverrideStore {
    return MemoryDefaultHoldOverrideStore(mutableMapOf<String, DefaultHoldOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultHoldOverrideStore(
    var inner: DefaultHoldOverrideStore,
    var cache: TtlCache<DefaultHoldOverride>,
    var hits: Int,
    var misses: Int,
) : DefaultHoldOverrideStore {
    override fun find(code: String): DefaultHoldOverride? {
        val hit = (cache.get(code) as DefaultHoldOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultHoldOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultHoldOverride> = inner.list()
}

fun newCachedDefaultHoldOverrideStore(inner: DefaultHoldOverrideStore, ttl: Int, clock: Clock): DefaultHoldOverrideStore {
    return CachedDefaultHoldOverrideStore(
        inner = inner,
        cache = newTtlCache<DefaultHoldOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default hold overrides. Empty fields match all. */
data class DefaultHoldOverrideQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultHoldOverride): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultHoldOverride>): MutableList<DefaultHoldOverride> {
        val matched = filter(items) { candidate: DefaultHoldOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultHoldOverrides(store: DefaultHoldOverrideStore, query: DefaultHoldOverrideQuery): MutableList<DefaultHoldOverride> {
    return query.apply(store.list())
}
