package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card checkpoints by code. */
interface RateCardCheckpointStore {
    fun find(code: String): RateCardCheckpoint?
    fun save(item: RateCardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardCheckpointStore(
    var items: MutableMap<String, RateCardCheckpoint>,
    var order: MutableList<String>,
) : RateCardCheckpointStore {
    override fun find(code: String): RateCardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardCheckpoint) {
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

    override fun list(): MutableList<RateCardCheckpoint> {
        val out = mutableListOf<RateCardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardCheckpointStore(): RateCardCheckpointStore {
    return MemoryRateCardCheckpointStore(mutableMapOf<String, RateCardCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardCheckpointStore(
    var inner: RateCardCheckpointStore,
    var cache: TtlCache<RateCardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RateCardCheckpointStore {
    override fun find(code: String): RateCardCheckpoint? {
        val hit = (cache.get(code) as RateCardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardCheckpoint> = inner.list()
}

fun newCachedRateCardCheckpointStore(inner: RateCardCheckpointStore, ttl: Int, clock: Clock): RateCardCheckpointStore {
    return CachedRateCardCheckpointStore(
        inner = inner,
        cache = newTtlCache<RateCardCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card checkpoints. Empty fields match all. */
data class RateCardCheckpointQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardCheckpoint>): MutableList<RateCardCheckpoint> {
        val matched = filter(items) { candidate: RateCardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardCheckpoints(store: RateCardCheckpointStore, query: RateCardCheckpointQuery): MutableList<RateCardCheckpoint> {
    return query.apply(store.list())
}
