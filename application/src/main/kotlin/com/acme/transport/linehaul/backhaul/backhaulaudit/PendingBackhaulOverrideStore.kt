package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending backhaul overrides by code. */
interface PendingBackhaulOverrideStore {
    fun find(code: String): PendingBackhaulOverride?
    fun save(item: PendingBackhaulOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBackhaulOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBackhaulOverrideStore(
    var items: MutableMap<String, PendingBackhaulOverride>,
    var order: MutableList<String>,
) : PendingBackhaulOverrideStore {
    override fun find(code: String): PendingBackhaulOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBackhaulOverride) {
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

    override fun list(): MutableList<PendingBackhaulOverride> {
        val out = mutableListOf<PendingBackhaulOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBackhaulOverrideStore(): PendingBackhaulOverrideStore {
    return MemoryPendingBackhaulOverrideStore(
        mutableMapOf<String, PendingBackhaulOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBackhaulOverrideStore(
    var inner: PendingBackhaulOverrideStore,
    var cache: TtlCache<PendingBackhaulOverride>,
    var hits: Int,
    var misses: Int,
) : PendingBackhaulOverrideStore {
    override fun find(code: String): PendingBackhaulOverride? {
        val hit = (cache.get(code) as PendingBackhaulOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBackhaulOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBackhaulOverride> = inner.list()
}

fun newCachedPendingBackhaulOverrideStore(inner: PendingBackhaulOverrideStore, ttl: Int, clock: Clock): PendingBackhaulOverrideStore {
    return CachedPendingBackhaulOverrideStore(
        inner = inner,
        cache = newTtlCache<PendingBackhaulOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending backhaul overrides. Empty fields match all. */
data class PendingBackhaulOverrideQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBackhaulOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingBackhaulOverride>): MutableList<PendingBackhaulOverride> {
        val matched = filter(items) { candidate: PendingBackhaulOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBackhaulOverrides(store: PendingBackhaulOverrideStore, query: PendingBackhaulOverrideQuery): MutableList<PendingBackhaulOverride> {
    return query.apply(store.list())
}
