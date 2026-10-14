package com.acme.transport.network.node.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global node overrides by code. */
interface GlobalNodeOverrideStore {
    fun find(code: String): GlobalNodeOverride?
    fun save(item: GlobalNodeOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalNodeOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalNodeOverrideStore(
    var items: MutableMap<String, GlobalNodeOverride>,
    var order: MutableList<String>,
) : GlobalNodeOverrideStore {
    override fun find(code: String): GlobalNodeOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalNodeOverride) {
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

    override fun list(): MutableList<GlobalNodeOverride> {
        val out = mutableListOf<GlobalNodeOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalNodeOverrideStore(): GlobalNodeOverrideStore {
    return MemoryGlobalNodeOverrideStore(mutableMapOf<String, GlobalNodeOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalNodeOverrideStore(
    var inner: GlobalNodeOverrideStore,
    var cache: TtlCache<GlobalNodeOverride>,
    var hits: Int,
    var misses: Int,
) : GlobalNodeOverrideStore {
    override fun find(code: String): GlobalNodeOverride? {
        val hit = (cache.get(code) as GlobalNodeOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalNodeOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalNodeOverride> = inner.list()
}

fun newCachedGlobalNodeOverrideStore(inner: GlobalNodeOverrideStore, ttl: Int, clock: Clock): GlobalNodeOverrideStore {
    return CachedGlobalNodeOverrideStore(
        inner = inner,
        cache = newTtlCache<GlobalNodeOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global node overrides. Empty fields match all. */
data class GlobalNodeOverrideQuery(
    var source: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalNodeOverride): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalNodeOverride>): MutableList<GlobalNodeOverride> {
        val matched = filter(items) { candidate: GlobalNodeOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalNodeOverrides(store: GlobalNodeOverrideStore, query: GlobalNodeOverrideQuery): MutableList<GlobalNodeOverride> {
    return query.apply(store.list())
}
