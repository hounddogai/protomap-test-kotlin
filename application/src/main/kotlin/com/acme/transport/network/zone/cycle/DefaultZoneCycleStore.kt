package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default zone cycles by code. */
interface DefaultZoneCycleStore {
    fun find(code: String): DefaultZoneCycle?
    fun save(item: DefaultZoneCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultZoneCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultZoneCycleStore(
    var items: MutableMap<String, DefaultZoneCycle>,
    var order: MutableList<String>,
) : DefaultZoneCycleStore {
    override fun find(code: String): DefaultZoneCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultZoneCycle) {
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

    override fun list(): MutableList<DefaultZoneCycle> {
        val out = mutableListOf<DefaultZoneCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultZoneCycleStore(): DefaultZoneCycleStore {
    return MemoryDefaultZoneCycleStore(mutableMapOf<String, DefaultZoneCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultZoneCycleStore(
    var inner: DefaultZoneCycleStore,
    var cache: TtlCache<DefaultZoneCycle>,
    var hits: Int,
    var misses: Int,
) : DefaultZoneCycleStore {
    override fun find(code: String): DefaultZoneCycle? {
        val hit = (cache.get(code) as DefaultZoneCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultZoneCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultZoneCycle> = inner.list()
}

fun newCachedDefaultZoneCycleStore(inner: DefaultZoneCycleStore, ttl: Int, clock: Clock): DefaultZoneCycleStore {
    return CachedDefaultZoneCycleStore(
        inner = inner,
        cache = newTtlCache<DefaultZoneCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default zone cycles. Empty fields match all. */
data class DefaultZoneCycleQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultZoneCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultZoneCycle>): MutableList<DefaultZoneCycle> {
        val matched = filter(items) { candidate: DefaultZoneCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultZoneCycles(store: DefaultZoneCycleStore, query: DefaultZoneCycleQuery): MutableList<DefaultZoneCycle> {
    return query.apply(store.list())
}
