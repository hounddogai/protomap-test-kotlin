package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global cutoff rollups by code. */
interface GlobalCutoffRollupStore {
    fun find(code: String): GlobalCutoffRollup?
    fun save(item: GlobalCutoffRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCutoffRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCutoffRollupStore(
    var items: MutableMap<String, GlobalCutoffRollup>,
    var order: MutableList<String>,
) : GlobalCutoffRollupStore {
    override fun find(code: String): GlobalCutoffRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCutoffRollup) {
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

    override fun list(): MutableList<GlobalCutoffRollup> {
        val out = mutableListOf<GlobalCutoffRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCutoffRollupStore(): GlobalCutoffRollupStore {
    return MemoryGlobalCutoffRollupStore(mutableMapOf<String, GlobalCutoffRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCutoffRollupStore(
    var inner: GlobalCutoffRollupStore,
    var cache: TtlCache<GlobalCutoffRollup>,
    var hits: Int,
    var misses: Int,
) : GlobalCutoffRollupStore {
    override fun find(code: String): GlobalCutoffRollup? {
        val hit = (cache.get(code) as GlobalCutoffRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCutoffRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCutoffRollup> = inner.list()
}

fun newCachedGlobalCutoffRollupStore(inner: GlobalCutoffRollupStore, ttl: Int, clock: Clock): GlobalCutoffRollupStore {
    return CachedGlobalCutoffRollupStore(
        inner = inner,
        cache = newTtlCache<GlobalCutoffRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cutoff rollups. Empty fields match all. */
data class GlobalCutoffRollupQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCutoffRollup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCutoffRollup>): MutableList<GlobalCutoffRollup> {
        val matched = filter(items) { candidate: GlobalCutoffRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCutoffRollups(store: GlobalCutoffRollupStore, query: GlobalCutoffRollupQuery): MutableList<GlobalCutoffRollup> {
    return query.apply(store.list())
}
