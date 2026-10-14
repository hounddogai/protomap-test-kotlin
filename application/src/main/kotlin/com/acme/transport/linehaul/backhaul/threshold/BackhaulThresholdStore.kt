package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul thresholds by code. */
interface BackhaulThresholdStore {
    fun find(code: String): BackhaulThreshold?
    fun save(item: BackhaulThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulThresholdStore(
    var items: MutableMap<String, BackhaulThreshold>,
    var order: MutableList<String>,
) : BackhaulThresholdStore {
    override fun find(code: String): BackhaulThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulThreshold) {
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

    override fun list(): MutableList<BackhaulThreshold> {
        val out = mutableListOf<BackhaulThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulThresholdStore(): BackhaulThresholdStore {
    return MemoryBackhaulThresholdStore(mutableMapOf<String, BackhaulThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulThresholdStore(
    var inner: BackhaulThresholdStore,
    var cache: TtlCache<BackhaulThreshold>,
    var hits: Int,
    var misses: Int,
) : BackhaulThresholdStore {
    override fun find(code: String): BackhaulThreshold? {
        val hit = (cache.get(code) as BackhaulThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulThreshold> = inner.list()
}

fun newCachedBackhaulThresholdStore(inner: BackhaulThresholdStore, ttl: Int, clock: Clock): BackhaulThresholdStore {
    return CachedBackhaulThresholdStore(
        inner = inner,
        cache = newTtlCache<BackhaulThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul thresholds. Empty fields match all. */
data class BackhaulThresholdQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulThreshold>): MutableList<BackhaulThreshold> {
        val matched = filter(items) { candidate: BackhaulThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulThresholds(store: BackhaulThresholdStore, query: BackhaulThresholdQuery): MutableList<BackhaulThreshold> {
    return query.apply(store.list())
}
