package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default near miss cycles by code. */
interface DefaultNearMissCycleStore {
    fun find(code: String): DefaultNearMissCycle?
    fun save(item: DefaultNearMissCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultNearMissCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultNearMissCycleStore(
    var items: MutableMap<String, DefaultNearMissCycle>,
    var order: MutableList<String>,
) : DefaultNearMissCycleStore {
    override fun find(code: String): DefaultNearMissCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultNearMissCycle) {
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

    override fun list(): MutableList<DefaultNearMissCycle> {
        val out = mutableListOf<DefaultNearMissCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultNearMissCycleStore(): DefaultNearMissCycleStore {
    return MemoryDefaultNearMissCycleStore(
        mutableMapOf<String, DefaultNearMissCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultNearMissCycleStore(
    var inner: DefaultNearMissCycleStore,
    var cache: TtlCache<DefaultNearMissCycle>,
    var hits: Int,
    var misses: Int,
) : DefaultNearMissCycleStore {
    override fun find(code: String): DefaultNearMissCycle? {
        val hit = (cache.get(code) as DefaultNearMissCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultNearMissCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultNearMissCycle> = inner.list()
}

fun newCachedDefaultNearMissCycleStore(inner: DefaultNearMissCycleStore, ttl: Int, clock: Clock): DefaultNearMissCycleStore {
    return CachedDefaultNearMissCycleStore(
        inner = inner,
        cache = newTtlCache<DefaultNearMissCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default near miss cycles. Empty fields match all. */
data class DefaultNearMissCycleQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultNearMissCycle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultNearMissCycle>): MutableList<DefaultNearMissCycle> {
        val matched = filter(items) { candidate: DefaultNearMissCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultNearMissCycles(store: DefaultNearMissCycleStore, query: DefaultNearMissCycleQuery): MutableList<DefaultNearMissCycle> {
    return query.apply(store.list())
}
