package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay thresholds by code. */
interface RelayThresholdStore {
    fun find(code: String): RelayThreshold?
    fun save(item: RelayThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayThresholdStore(
    var items: MutableMap<String, RelayThreshold>,
    var order: MutableList<String>,
) : RelayThresholdStore {
    override fun find(code: String): RelayThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayThreshold) {
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

    override fun list(): MutableList<RelayThreshold> {
        val out = mutableListOf<RelayThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayThresholdStore(): RelayThresholdStore {
    return MemoryRelayThresholdStore(mutableMapOf<String, RelayThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayThresholdStore(
    var inner: RelayThresholdStore,
    var cache: TtlCache<RelayThreshold>,
    var hits: Int,
    var misses: Int,
) : RelayThresholdStore {
    override fun find(code: String): RelayThreshold? {
        val hit = (cache.get(code) as RelayThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayThreshold> = inner.list()
}

fun newCachedRelayThresholdStore(inner: RelayThresholdStore, ttl: Int, clock: Clock): RelayThresholdStore {
    return CachedRelayThresholdStore(
        inner = inner,
        cache = newTtlCache<RelayThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay thresholds. Empty fields match all. */
data class RelayThresholdQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RelayThreshold>): MutableList<RelayThreshold> {
        val matched = filter(items) { candidate: RelayThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayThresholds(store: RelayThresholdStore, query: RelayThresholdQuery): MutableList<RelayThreshold> {
    return query.apply(store.list())
}
