package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation scores by code. */
interface ViolationScoreStore {
    fun find(code: String): ViolationScore?
    fun save(item: ViolationScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationScoreStore(
    var items: MutableMap<String, ViolationScore>,
    var order: MutableList<String>,
) : ViolationScoreStore {
    override fun find(code: String): ViolationScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationScore) {
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

    override fun list(): MutableList<ViolationScore> {
        val out = mutableListOf<ViolationScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationScoreStore(): ViolationScoreStore {
    return MemoryViolationScoreStore(mutableMapOf<String, ViolationScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationScoreStore(
    var inner: ViolationScoreStore,
    var cache: TtlCache<ViolationScore>,
    var hits: Int,
    var misses: Int,
) : ViolationScoreStore {
    override fun find(code: String): ViolationScore? {
        val hit = (cache.get(code) as ViolationScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationScore> = inner.list()
}

fun newCachedViolationScoreStore(inner: ViolationScoreStore, ttl: Int, clock: Clock): ViolationScoreStore {
    return CachedViolationScoreStore(
        inner = inner,
        cache = newTtlCache<ViolationScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation scores. Empty fields match all. */
data class ViolationScoreQuery(
    var unit: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ViolationScore>): MutableList<ViolationScore> {
        val matched = filter(items) { candidate: ViolationScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationScores(store: ViolationScoreStore, query: ViolationScoreQuery): MutableList<ViolationScore> {
    return query.apply(store.list())
}
