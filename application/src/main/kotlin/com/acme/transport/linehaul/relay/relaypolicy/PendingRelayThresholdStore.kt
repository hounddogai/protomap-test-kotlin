package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending relay thresholds by code. */
interface PendingRelayThresholdStore {
    fun find(code: String): PendingRelayThreshold?
    fun save(item: PendingRelayThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRelayThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRelayThresholdStore(
    var items: MutableMap<String, PendingRelayThreshold>,
    var order: MutableList<String>,
) : PendingRelayThresholdStore {
    override fun find(code: String): PendingRelayThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRelayThreshold) {
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

    override fun list(): MutableList<PendingRelayThreshold> {
        val out = mutableListOf<PendingRelayThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRelayThresholdStore(): PendingRelayThresholdStore {
    return MemoryPendingRelayThresholdStore(
        mutableMapOf<String, PendingRelayThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRelayThresholdStore(
    var inner: PendingRelayThresholdStore,
    var cache: TtlCache<PendingRelayThreshold>,
    var hits: Int,
    var misses: Int,
) : PendingRelayThresholdStore {
    override fun find(code: String): PendingRelayThreshold? {
        val hit = (cache.get(code) as PendingRelayThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRelayThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRelayThreshold> = inner.list()
}

fun newCachedPendingRelayThresholdStore(inner: PendingRelayThresholdStore, ttl: Int, clock: Clock): PendingRelayThresholdStore {
    return CachedPendingRelayThresholdStore(
        inner = inner,
        cache = newTtlCache<PendingRelayThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending relay thresholds. Empty fields match all. */
data class PendingRelayThresholdQuery(
    var channel: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRelayThreshold): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRelayThreshold>): MutableList<PendingRelayThreshold> {
        val matched = filter(items) { candidate: PendingRelayThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRelayThresholds(store: PendingRelayThresholdStore, query: PendingRelayThresholdQuery): MutableList<PendingRelayThreshold> {
    return query.apply(store.list())
}
