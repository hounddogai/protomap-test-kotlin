package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual capacity exceptions by code. */
interface ManualCapacityExceptionStore {
    fun find(code: String): ManualCapacityException?
    fun save(item: ManualCapacityException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCapacityException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCapacityExceptionStore(
    var items: MutableMap<String, ManualCapacityException>,
    var order: MutableList<String>,
) : ManualCapacityExceptionStore {
    override fun find(code: String): ManualCapacityException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCapacityException) {
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

    override fun list(): MutableList<ManualCapacityException> {
        val out = mutableListOf<ManualCapacityException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCapacityExceptionStore(): ManualCapacityExceptionStore {
    return MemoryManualCapacityExceptionStore(
        mutableMapOf<String, ManualCapacityException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCapacityExceptionStore(
    var inner: ManualCapacityExceptionStore,
    var cache: TtlCache<ManualCapacityException>,
    var hits: Int,
    var misses: Int,
) : ManualCapacityExceptionStore {
    override fun find(code: String): ManualCapacityException? {
        val hit = (cache.get(code) as ManualCapacityException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCapacityException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCapacityException> = inner.list()
}

fun newCachedManualCapacityExceptionStore(inner: ManualCapacityExceptionStore, ttl: Int, clock: Clock): ManualCapacityExceptionStore {
    return CachedManualCapacityExceptionStore(
        inner = inner,
        cache = newTtlCache<ManualCapacityException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual capacity exceptions. Empty fields match all. */
data class ManualCapacityExceptionQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCapacityException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCapacityException>): MutableList<ManualCapacityException> {
        val matched = filter(items) { candidate: ManualCapacityException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCapacityExceptions(store: ManualCapacityExceptionStore, query: ManualCapacityExceptionQuery): MutableList<ManualCapacityException> {
    return query.apply(store.list())
}
