package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop thresholds by code. */
interface DropThresholdStore {
    fun find(code: String): DropThreshold?
    fun save(item: DropThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropThresholdStore(
    var items: MutableMap<String, DropThreshold>,
    var order: MutableList<String>,
) : DropThresholdStore {
    override fun find(code: String): DropThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropThreshold) {
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

    override fun list(): MutableList<DropThreshold> {
        val out = mutableListOf<DropThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropThresholdStore(): DropThresholdStore {
    return MemoryDropThresholdStore(mutableMapOf<String, DropThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropThresholdStore(
    var inner: DropThresholdStore,
    var cache: TtlCache<DropThreshold>,
    var hits: Int,
    var misses: Int,
) : DropThresholdStore {
    override fun find(code: String): DropThreshold? {
        val hit = (cache.get(code) as DropThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropThreshold> = inner.list()
}

fun newCachedDropThresholdStore(inner: DropThresholdStore, ttl: Int, clock: Clock): DropThresholdStore {
    return CachedDropThresholdStore(
        inner = inner,
        cache = newTtlCache<DropThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop thresholds. Empty fields match all. */
data class DropThresholdQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropThreshold>): MutableList<DropThreshold> {
        val matched = filter(items) { candidate: DropThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropThresholds(store: DropThresholdStore, query: DropThresholdQuery): MutableList<DropThreshold> {
    return query.apply(store.list())
}
