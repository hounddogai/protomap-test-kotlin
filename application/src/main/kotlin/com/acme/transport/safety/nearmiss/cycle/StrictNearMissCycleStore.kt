package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict near miss cycles by code. */
interface StrictNearMissCycleStore {
    fun find(code: String): StrictNearMissCycle?
    fun save(item: StrictNearMissCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictNearMissCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictNearMissCycleStore(
    var items: MutableMap<String, StrictNearMissCycle>,
    var order: MutableList<String>,
) : StrictNearMissCycleStore {
    override fun find(code: String): StrictNearMissCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictNearMissCycle) {
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

    override fun list(): MutableList<StrictNearMissCycle> {
        val out = mutableListOf<StrictNearMissCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictNearMissCycleStore(): StrictNearMissCycleStore {
    return MemoryStrictNearMissCycleStore(mutableMapOf<String, StrictNearMissCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictNearMissCycleStore(
    var inner: StrictNearMissCycleStore,
    var cache: TtlCache<StrictNearMissCycle>,
    var hits: Int,
    var misses: Int,
) : StrictNearMissCycleStore {
    override fun find(code: String): StrictNearMissCycle? {
        val hit = (cache.get(code) as StrictNearMissCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictNearMissCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictNearMissCycle> = inner.list()
}

fun newCachedStrictNearMissCycleStore(inner: StrictNearMissCycleStore, ttl: Int, clock: Clock): StrictNearMissCycleStore {
    return CachedStrictNearMissCycleStore(
        inner = inner,
        cache = newTtlCache<StrictNearMissCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict near miss cycles. Empty fields match all. */
data class StrictNearMissCycleQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictNearMissCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictNearMissCycle>): MutableList<StrictNearMissCycle> {
        val matched = filter(items) { candidate: StrictNearMissCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictNearMissCycles(store: StrictNearMissCycleStore, query: StrictNearMissCycleQuery): MutableList<StrictNearMissCycle> {
    return query.apply(store.list())
}
