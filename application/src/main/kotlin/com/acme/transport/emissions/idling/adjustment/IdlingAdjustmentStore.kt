package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling adjustments by code. */
interface IdlingAdjustmentStore {
    fun find(code: String): IdlingAdjustment?
    fun save(item: IdlingAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingAdjustmentStore(
    var items: MutableMap<String, IdlingAdjustment>,
    var order: MutableList<String>,
) : IdlingAdjustmentStore {
    override fun find(code: String): IdlingAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingAdjustment) {
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

    override fun list(): MutableList<IdlingAdjustment> {
        val out = mutableListOf<IdlingAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingAdjustmentStore(): IdlingAdjustmentStore {
    return MemoryIdlingAdjustmentStore(mutableMapOf<String, IdlingAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingAdjustmentStore(
    var inner: IdlingAdjustmentStore,
    var cache: TtlCache<IdlingAdjustment>,
    var hits: Int,
    var misses: Int,
) : IdlingAdjustmentStore {
    override fun find(code: String): IdlingAdjustment? {
        val hit = (cache.get(code) as IdlingAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingAdjustment> = inner.list()
}

fun newCachedIdlingAdjustmentStore(inner: IdlingAdjustmentStore, ttl: Int, clock: Clock): IdlingAdjustmentStore {
    return CachedIdlingAdjustmentStore(
        inner = inner,
        cache = newTtlCache<IdlingAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling adjustments. Empty fields match all. */
data class IdlingAdjustmentQuery(
    var locale: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingAdjustment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<IdlingAdjustment>): MutableList<IdlingAdjustment> {
        val matched = filter(items) { candidate: IdlingAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingAdjustments(store: IdlingAdjustmentStore, query: IdlingAdjustmentQuery): MutableList<IdlingAdjustment> {
    return query.apply(store.list())
}
