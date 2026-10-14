package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling defect exceptions by code. */
interface RollingDefectExceptionStore {
    fun find(code: String): RollingDefectException?
    fun save(item: RollingDefectException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDefectException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDefectExceptionStore(
    var items: MutableMap<String, RollingDefectException>,
    var order: MutableList<String>,
) : RollingDefectExceptionStore {
    override fun find(code: String): RollingDefectException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDefectException) {
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

    override fun list(): MutableList<RollingDefectException> {
        val out = mutableListOf<RollingDefectException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDefectExceptionStore(): RollingDefectExceptionStore {
    return MemoryRollingDefectExceptionStore(
        mutableMapOf<String, RollingDefectException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDefectExceptionStore(
    var inner: RollingDefectExceptionStore,
    var cache: TtlCache<RollingDefectException>,
    var hits: Int,
    var misses: Int,
) : RollingDefectExceptionStore {
    override fun find(code: String): RollingDefectException? {
        val hit = (cache.get(code) as RollingDefectException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDefectException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDefectException> = inner.list()
}

fun newCachedRollingDefectExceptionStore(inner: RollingDefectExceptionStore, ttl: Int, clock: Clock): RollingDefectExceptionStore {
    return CachedRollingDefectExceptionStore(
        inner = inner,
        cache = newTtlCache<RollingDefectException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling defect exceptions. Empty fields match all. */
data class RollingDefectExceptionQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDefectException): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDefectException>): MutableList<RollingDefectException> {
        val matched = filter(items) { candidate: RollingDefectException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDefectExceptions(store: RollingDefectExceptionStore, query: RollingDefectExceptionQuery): MutableList<RollingDefectException> {
    return query.apply(store.list())
}
