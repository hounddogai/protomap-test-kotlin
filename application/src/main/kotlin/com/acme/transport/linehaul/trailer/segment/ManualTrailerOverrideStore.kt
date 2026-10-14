package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual trailer overrides by code. */
interface ManualTrailerOverrideStore {
    fun find(code: String): ManualTrailerOverride?
    fun save(item: ManualTrailerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerOverrideStore(
    var items: MutableMap<String, ManualTrailerOverride>,
    var order: MutableList<String>,
) : ManualTrailerOverrideStore {
    override fun find(code: String): ManualTrailerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerOverride) {
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

    override fun list(): MutableList<ManualTrailerOverride> {
        val out = mutableListOf<ManualTrailerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerOverrideStore(): ManualTrailerOverrideStore {
    return MemoryManualTrailerOverrideStore(
        mutableMapOf<String, ManualTrailerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerOverrideStore(
    var inner: ManualTrailerOverrideStore,
    var cache: TtlCache<ManualTrailerOverride>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerOverrideStore {
    override fun find(code: String): ManualTrailerOverride? {
        val hit = (cache.get(code) as ManualTrailerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerOverride> = inner.list()
}

fun newCachedManualTrailerOverrideStore(inner: ManualTrailerOverrideStore, ttl: Int, clock: Clock): ManualTrailerOverrideStore {
    return CachedManualTrailerOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer overrides. Empty fields match all. */
data class ManualTrailerOverrideQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTrailerOverride>): MutableList<ManualTrailerOverride> {
        val matched = filter(items) { candidate: ManualTrailerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerOverrides(store: ManualTrailerOverrideStore, query: ManualTrailerOverrideQuery): MutableList<ManualTrailerOverride> {
    return query.apply(store.list())
}
