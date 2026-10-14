package com.acme.transport.emissions.idling.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling cycles by code. */
interface IdlingCycleStore {
    fun find(code: String): IdlingCycle?
    fun save(item: IdlingCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingCycleStore(
    var items: MutableMap<String, IdlingCycle>,
    var order: MutableList<String>,
) : IdlingCycleStore {
    override fun find(code: String): IdlingCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingCycle) {
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

    override fun list(): MutableList<IdlingCycle> {
        val out = mutableListOf<IdlingCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingCycleStore(): IdlingCycleStore {
    return MemoryIdlingCycleStore(mutableMapOf<String, IdlingCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingCycleStore(
    var inner: IdlingCycleStore,
    var cache: TtlCache<IdlingCycle>,
    var hits: Int,
    var misses: Int,
) : IdlingCycleStore {
    override fun find(code: String): IdlingCycle? {
        val hit = (cache.get(code) as IdlingCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingCycle> = inner.list()
}

fun newCachedIdlingCycleStore(inner: IdlingCycleStore, ttl: Int, clock: Clock): IdlingCycleStore {
    return CachedIdlingCycleStore(
        inner = inner,
        cache = newTtlCache<IdlingCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling cycles. Empty fields match all. */
data class IdlingCycleQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<IdlingCycle>): MutableList<IdlingCycle> {
        val matched = filter(items) { candidate: IdlingCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingCycles(store: IdlingCycleStore, query: IdlingCycleQuery): MutableList<IdlingCycle> {
    return query.apply(store.list())
}
