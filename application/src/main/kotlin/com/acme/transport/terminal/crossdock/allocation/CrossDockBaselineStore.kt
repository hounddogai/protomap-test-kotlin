package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock baselines by code. */
interface CrossDockBaselineStore {
    fun find(code: String): CrossDockBaseline?
    fun save(item: CrossDockBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockBaselineStore(
    var items: MutableMap<String, CrossDockBaseline>,
    var order: MutableList<String>,
) : CrossDockBaselineStore {
    override fun find(code: String): CrossDockBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockBaseline) {
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

    override fun list(): MutableList<CrossDockBaseline> {
        val out = mutableListOf<CrossDockBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockBaselineStore(): CrossDockBaselineStore {
    return MemoryCrossDockBaselineStore(mutableMapOf<String, CrossDockBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockBaselineStore(
    var inner: CrossDockBaselineStore,
    var cache: TtlCache<CrossDockBaseline>,
    var hits: Int,
    var misses: Int,
) : CrossDockBaselineStore {
    override fun find(code: String): CrossDockBaseline? {
        val hit = (cache.get(code) as CrossDockBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockBaseline> = inner.list()
}

fun newCachedCrossDockBaselineStore(inner: CrossDockBaselineStore, ttl: Int, clock: Clock): CrossDockBaselineStore {
    return CachedCrossDockBaselineStore(
        inner = inner,
        cache = newTtlCache<CrossDockBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock baselines. Empty fields match all. */
data class CrossDockBaselineQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CrossDockBaseline>): MutableList<CrossDockBaseline> {
        val matched = filter(items) { candidate: CrossDockBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockBaselines(store: CrossDockBaselineStore, query: CrossDockBaselineQuery): MutableList<CrossDockBaseline> {
    return query.apply(store.list())
}
