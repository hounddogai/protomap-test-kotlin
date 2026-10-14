package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default service area rollups by code. */
interface DefaultServiceAreaRollupStore {
    fun find(code: String): DefaultServiceAreaRollup?
    fun save(item: DefaultServiceAreaRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultServiceAreaRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultServiceAreaRollupStore(
    var items: MutableMap<String, DefaultServiceAreaRollup>,
    var order: MutableList<String>,
) : DefaultServiceAreaRollupStore {
    override fun find(code: String): DefaultServiceAreaRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultServiceAreaRollup) {
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

    override fun list(): MutableList<DefaultServiceAreaRollup> {
        val out = mutableListOf<DefaultServiceAreaRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultServiceAreaRollupStore(): DefaultServiceAreaRollupStore {
    return MemoryDefaultServiceAreaRollupStore(
        mutableMapOf<String, DefaultServiceAreaRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultServiceAreaRollupStore(
    var inner: DefaultServiceAreaRollupStore,
    var cache: TtlCache<DefaultServiceAreaRollup>,
    var hits: Int,
    var misses: Int,
) : DefaultServiceAreaRollupStore {
    override fun find(code: String): DefaultServiceAreaRollup? {
        val hit = (cache.get(code) as DefaultServiceAreaRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultServiceAreaRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultServiceAreaRollup> = inner.list()
}

fun newCachedDefaultServiceAreaRollupStore(inner: DefaultServiceAreaRollupStore, ttl: Int, clock: Clock): DefaultServiceAreaRollupStore {
    return CachedDefaultServiceAreaRollupStore(
        inner = inner,
        cache = newTtlCache<DefaultServiceAreaRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default service area rollups. Empty fields match all. */
data class DefaultServiceAreaRollupQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultServiceAreaRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultServiceAreaRollup>): MutableList<DefaultServiceAreaRollup> {
        val matched = filter(items) { candidate: DefaultServiceAreaRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultServiceAreaRollups(store: DefaultServiceAreaRollupStore, query: DefaultServiceAreaRollupQuery): MutableList<DefaultServiceAreaRollup> {
    return query.apply(store.list())
}
