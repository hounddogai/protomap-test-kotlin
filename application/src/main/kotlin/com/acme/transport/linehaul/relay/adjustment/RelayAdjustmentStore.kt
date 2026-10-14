package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay adjustments by code. */
interface RelayAdjustmentStore {
    fun find(code: String): RelayAdjustment?
    fun save(item: RelayAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayAdjustmentStore(
    var items: MutableMap<String, RelayAdjustment>,
    var order: MutableList<String>,
) : RelayAdjustmentStore {
    override fun find(code: String): RelayAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayAdjustment) {
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

    override fun list(): MutableList<RelayAdjustment> {
        val out = mutableListOf<RelayAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayAdjustmentStore(): RelayAdjustmentStore {
    return MemoryRelayAdjustmentStore(mutableMapOf<String, RelayAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayAdjustmentStore(
    var inner: RelayAdjustmentStore,
    var cache: TtlCache<RelayAdjustment>,
    var hits: Int,
    var misses: Int,
) : RelayAdjustmentStore {
    override fun find(code: String): RelayAdjustment? {
        val hit = (cache.get(code) as RelayAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayAdjustment> = inner.list()
}

fun newCachedRelayAdjustmentStore(inner: RelayAdjustmentStore, ttl: Int, clock: Clock): RelayAdjustmentStore {
    return CachedRelayAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RelayAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay adjustments. Empty fields match all. */
data class RelayAdjustmentQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayAdjustment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RelayAdjustment>): MutableList<RelayAdjustment> {
        val matched = filter(items) { candidate: RelayAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayAdjustments(store: RelayAdjustmentStore, query: RelayAdjustmentQuery): MutableList<RelayAdjustment> {
    return query.apply(store.list())
}
