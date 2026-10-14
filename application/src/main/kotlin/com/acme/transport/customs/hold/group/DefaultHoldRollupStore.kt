package com.acme.transport.customs.hold.group

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default hold rollups by code. */
interface DefaultHoldRollupStore {
    fun find(code: String): DefaultHoldRollup?
    fun save(item: DefaultHoldRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultHoldRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultHoldRollupStore(
    var items: MutableMap<String, DefaultHoldRollup>,
    var order: MutableList<String>,
) : DefaultHoldRollupStore {
    override fun find(code: String): DefaultHoldRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultHoldRollup) {
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

    override fun list(): MutableList<DefaultHoldRollup> {
        val out = mutableListOf<DefaultHoldRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultHoldRollupStore(): DefaultHoldRollupStore {
    return MemoryDefaultHoldRollupStore(mutableMapOf<String, DefaultHoldRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultHoldRollupStore(
    var inner: DefaultHoldRollupStore,
    var cache: TtlCache<DefaultHoldRollup>,
    var hits: Int,
    var misses: Int,
) : DefaultHoldRollupStore {
    override fun find(code: String): DefaultHoldRollup? {
        val hit = (cache.get(code) as DefaultHoldRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultHoldRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultHoldRollup> = inner.list()
}

fun newCachedDefaultHoldRollupStore(inner: DefaultHoldRollupStore, ttl: Int, clock: Clock): DefaultHoldRollupStore {
    return CachedDefaultHoldRollupStore(
        inner = inner,
        cache = newTtlCache<DefaultHoldRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default hold rollups. Empty fields match all. */
data class DefaultHoldRollupQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultHoldRollup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultHoldRollup>): MutableList<DefaultHoldRollup> {
        val matched = filter(items) { candidate: DefaultHoldRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultHoldRollups(store: DefaultHoldRollupStore, query: DefaultHoldRollupQuery): MutableList<DefaultHoldRollup> {
    return query.apply(store.list())
}
