package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling violation batches by code. */
interface RollingViolationBatchStore {
    fun find(code: String): RollingViolationBatch?
    fun save(item: RollingViolationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingViolationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingViolationBatchStore(
    var items: MutableMap<String, RollingViolationBatch>,
    var order: MutableList<String>,
) : RollingViolationBatchStore {
    override fun find(code: String): RollingViolationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingViolationBatch) {
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

    override fun list(): MutableList<RollingViolationBatch> {
        val out = mutableListOf<RollingViolationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingViolationBatchStore(): RollingViolationBatchStore {
    return MemoryRollingViolationBatchStore(
        mutableMapOf<String, RollingViolationBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingViolationBatchStore(
    var inner: RollingViolationBatchStore,
    var cache: TtlCache<RollingViolationBatch>,
    var hits: Int,
    var misses: Int,
) : RollingViolationBatchStore {
    override fun find(code: String): RollingViolationBatch? {
        val hit = (cache.get(code) as RollingViolationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingViolationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingViolationBatch> = inner.list()
}

fun newCachedRollingViolationBatchStore(inner: RollingViolationBatchStore, ttl: Int, clock: Clock): RollingViolationBatchStore {
    return CachedRollingViolationBatchStore(
        inner = inner,
        cache = newTtlCache<RollingViolationBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling violation batches. Empty fields match all. */
data class RollingViolationBatchQuery(
    var locale: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingViolationBatch): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<RollingViolationBatch>): MutableList<RollingViolationBatch> {
        val matched = filter(items) { candidate: RollingViolationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingViolationBatches(store: RollingViolationBatchStore, query: RollingViolationBatchQuery): MutableList<RollingViolationBatch> {
    return query.apply(store.list())
}
