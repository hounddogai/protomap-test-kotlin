package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss cycles by code. */
interface NearMissCycleStore {
    fun find(code: String): NearMissCycle?
    fun save(item: NearMissCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissCycleStore(
    var items: MutableMap<String, NearMissCycle>,
    var order: MutableList<String>,
) : NearMissCycleStore {
    override fun find(code: String): NearMissCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissCycle) {
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

    override fun list(): MutableList<NearMissCycle> {
        val out = mutableListOf<NearMissCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissCycleStore(): NearMissCycleStore {
    return MemoryNearMissCycleStore(mutableMapOf<String, NearMissCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissCycleStore(
    var inner: NearMissCycleStore,
    var cache: TtlCache<NearMissCycle>,
    var hits: Int,
    var misses: Int,
) : NearMissCycleStore {
    override fun find(code: String): NearMissCycle? {
        val hit = (cache.get(code) as NearMissCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissCycle> = inner.list()
}

fun newCachedNearMissCycleStore(inner: NearMissCycleStore, ttl: Int, clock: Clock): NearMissCycleStore {
    return CachedNearMissCycleStore(
        inner = inner,
        cache = newTtlCache<NearMissCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss cycles. Empty fields match all. */
data class NearMissCycleQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<NearMissCycle>): MutableList<NearMissCycle> {
        val matched = filter(items) { candidate: NearMissCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissCycles(store: NearMissCycleStore, query: NearMissCycleQuery): MutableList<NearMissCycle> {
    return query.apply(store.list())
}
