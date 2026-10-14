package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge scores by code. */
interface EvChargeScoreStore {
    fun find(code: String): EvChargeScore?
    fun save(item: EvChargeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeScoreStore(
    var items: MutableMap<String, EvChargeScore>,
    var order: MutableList<String>,
) : EvChargeScoreStore {
    override fun find(code: String): EvChargeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeScore) {
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

    override fun list(): MutableList<EvChargeScore> {
        val out = mutableListOf<EvChargeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeScoreStore(): EvChargeScoreStore {
    return MemoryEvChargeScoreStore(mutableMapOf<String, EvChargeScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeScoreStore(
    var inner: EvChargeScoreStore,
    var cache: TtlCache<EvChargeScore>,
    var hits: Int,
    var misses: Int,
) : EvChargeScoreStore {
    override fun find(code: String): EvChargeScore? {
        val hit = (cache.get(code) as EvChargeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeScore> = inner.list()
}

fun newCachedEvChargeScoreStore(inner: EvChargeScoreStore, ttl: Int, clock: Clock): EvChargeScoreStore {
    return CachedEvChargeScoreStore(
        inner = inner,
        cache = newTtlCache<EvChargeScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge scores. Empty fields match all. */
data class EvChargeScoreQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeScore>): MutableList<EvChargeScore> {
        val matched = filter(items) { candidate: EvChargeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeScores(store: EvChargeScoreStore, query: EvChargeScoreQuery): MutableList<EvChargeScore> {
    return query.apply(store.list())
}
