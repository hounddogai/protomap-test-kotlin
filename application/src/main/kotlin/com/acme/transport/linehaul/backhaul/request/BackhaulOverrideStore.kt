package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul overrides by code. */
interface BackhaulOverrideStore {
    fun find(code: String): BackhaulOverride?
    fun save(item: BackhaulOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulOverrideStore(
    var items: MutableMap<String, BackhaulOverride>,
    var order: MutableList<String>,
) : BackhaulOverrideStore {
    override fun find(code: String): BackhaulOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulOverride) {
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

    override fun list(): MutableList<BackhaulOverride> {
        val out = mutableListOf<BackhaulOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulOverrideStore(): BackhaulOverrideStore {
    return MemoryBackhaulOverrideStore(mutableMapOf<String, BackhaulOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulOverrideStore(
    var inner: BackhaulOverrideStore,
    var cache: TtlCache<BackhaulOverride>,
    var hits: Int,
    var misses: Int,
) : BackhaulOverrideStore {
    override fun find(code: String): BackhaulOverride? {
        val hit = (cache.get(code) as BackhaulOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulOverride> = inner.list()
}

fun newCachedBackhaulOverrideStore(inner: BackhaulOverrideStore, ttl: Int, clock: Clock): BackhaulOverrideStore {
    return CachedBackhaulOverrideStore(
        inner = inner,
        cache = newTtlCache<BackhaulOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul overrides. Empty fields match all. */
data class BackhaulOverrideQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulOverride>): MutableList<BackhaulOverride> {
        val matched = filter(items) { candidate: BackhaulOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulOverrides(store: BackhaulOverrideStore, query: BackhaulOverrideQuery): MutableList<BackhaulOverride> {
    return query.apply(store.list())
}
