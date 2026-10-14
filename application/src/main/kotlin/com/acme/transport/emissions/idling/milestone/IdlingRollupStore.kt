package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling rollups by code. */
interface IdlingRollupStore {
    fun find(code: String): IdlingRollup?
    fun save(item: IdlingRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingRollupStore(
    var items: MutableMap<String, IdlingRollup>,
    var order: MutableList<String>,
) : IdlingRollupStore {
    override fun find(code: String): IdlingRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingRollup) {
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

    override fun list(): MutableList<IdlingRollup> {
        val out = mutableListOf<IdlingRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingRollupStore(): IdlingRollupStore {
    return MemoryIdlingRollupStore(mutableMapOf<String, IdlingRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingRollupStore(
    var inner: IdlingRollupStore,
    var cache: TtlCache<IdlingRollup>,
    var hits: Int,
    var misses: Int,
) : IdlingRollupStore {
    override fun find(code: String): IdlingRollup? {
        val hit = (cache.get(code) as IdlingRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingRollup> = inner.list()
}

fun newCachedIdlingRollupStore(inner: IdlingRollupStore, ttl: Int, clock: Clock): IdlingRollupStore {
    return CachedIdlingRollupStore(
        inner = inner,
        cache = newTtlCache<IdlingRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling rollups. Empty fields match all. */
data class IdlingRollupQuery(
    var unit: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingRollup>): MutableList<IdlingRollup> {
        val matched = filter(items) { candidate: IdlingRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingRollups(store: IdlingRollupStore, query: IdlingRollupQuery): MutableList<IdlingRollup> {
    return query.apply(store.list())
}
