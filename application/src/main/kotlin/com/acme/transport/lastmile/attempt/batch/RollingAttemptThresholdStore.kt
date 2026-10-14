package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling attempt thresholds by code. */
interface RollingAttemptThresholdStore {
    fun find(code: String): RollingAttemptThreshold?
    fun save(item: RollingAttemptThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAttemptThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAttemptThresholdStore(
    var items: MutableMap<String, RollingAttemptThreshold>,
    var order: MutableList<String>,
) : RollingAttemptThresholdStore {
    override fun find(code: String): RollingAttemptThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAttemptThreshold) {
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

    override fun list(): MutableList<RollingAttemptThreshold> {
        val out = mutableListOf<RollingAttemptThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAttemptThresholdStore(): RollingAttemptThresholdStore {
    return MemoryRollingAttemptThresholdStore(
        mutableMapOf<String, RollingAttemptThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAttemptThresholdStore(
    var inner: RollingAttemptThresholdStore,
    var cache: TtlCache<RollingAttemptThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingAttemptThresholdStore {
    override fun find(code: String): RollingAttemptThreshold? {
        val hit = (cache.get(code) as RollingAttemptThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAttemptThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAttemptThreshold> = inner.list()
}

fun newCachedRollingAttemptThresholdStore(inner: RollingAttemptThresholdStore, ttl: Int, clock: Clock): RollingAttemptThresholdStore {
    return CachedRollingAttemptThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingAttemptThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling attempt thresholds. Empty fields match all. */
data class RollingAttemptThresholdQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAttemptThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAttemptThreshold>): MutableList<RollingAttemptThreshold> {
        val matched = filter(items) { candidate: RollingAttemptThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAttemptThresholds(store: RollingAttemptThresholdStore, query: RollingAttemptThresholdQuery): MutableList<RollingAttemptThreshold> {
    return query.apply(store.list())
}
