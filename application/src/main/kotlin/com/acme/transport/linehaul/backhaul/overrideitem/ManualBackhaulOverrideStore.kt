package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual backhaul overrides by code. */
interface ManualBackhaulOverrideStore {
    fun find(code: String): ManualBackhaulOverride?
    fun save(item: ManualBackhaulOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualBackhaulOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualBackhaulOverrideStore(
    var items: MutableMap<String, ManualBackhaulOverride>,
    var order: MutableList<String>,
) : ManualBackhaulOverrideStore {
    override fun find(code: String): ManualBackhaulOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualBackhaulOverride) {
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

    override fun list(): MutableList<ManualBackhaulOverride> {
        val out = mutableListOf<ManualBackhaulOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualBackhaulOverrideStore(): ManualBackhaulOverrideStore {
    return MemoryManualBackhaulOverrideStore(
        mutableMapOf<String, ManualBackhaulOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualBackhaulOverrideStore(
    var inner: ManualBackhaulOverrideStore,
    var cache: TtlCache<ManualBackhaulOverride>,
    var hits: Int,
    var misses: Int,
) : ManualBackhaulOverrideStore {
    override fun find(code: String): ManualBackhaulOverride? {
        val hit = (cache.get(code) as ManualBackhaulOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualBackhaulOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualBackhaulOverride> = inner.list()
}

fun newCachedManualBackhaulOverrideStore(inner: ManualBackhaulOverrideStore, ttl: Int, clock: Clock): ManualBackhaulOverrideStore {
    return CachedManualBackhaulOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualBackhaulOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual backhaul overrides. Empty fields match all. */
data class ManualBackhaulOverrideQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualBackhaulOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualBackhaulOverride>): MutableList<ManualBackhaulOverride> {
        val matched = filter(items) { candidate: ManualBackhaulOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualBackhaulOverrides(store: ManualBackhaulOverrideStore, query: ManualBackhaulOverrideQuery): MutableList<ManualBackhaulOverride> {
    return query.apply(store.list())
}
