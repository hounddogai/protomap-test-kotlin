package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional rate card checkpoints by code. */
interface RegionalRateCardCheckpointStore {
    fun find(code: String): RegionalRateCardCheckpoint?
    fun save(item: RegionalRateCardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRateCardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRateCardCheckpointStore(
    var items: MutableMap<String, RegionalRateCardCheckpoint>,
    var order: MutableList<String>,
) : RegionalRateCardCheckpointStore {
    override fun find(code: String): RegionalRateCardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRateCardCheckpoint) {
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

    override fun list(): MutableList<RegionalRateCardCheckpoint> {
        val out = mutableListOf<RegionalRateCardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRateCardCheckpointStore(): RegionalRateCardCheckpointStore {
    return MemoryRegionalRateCardCheckpointStore(
        mutableMapOf<String, RegionalRateCardCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRateCardCheckpointStore(
    var inner: RegionalRateCardCheckpointStore,
    var cache: TtlCache<RegionalRateCardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RegionalRateCardCheckpointStore {
    override fun find(code: String): RegionalRateCardCheckpoint? {
        val hit = (cache.get(code) as RegionalRateCardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRateCardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRateCardCheckpoint> = inner.list()
}

fun newCachedRegionalRateCardCheckpointStore(inner: RegionalRateCardCheckpointStore, ttl: Int, clock: Clock): RegionalRateCardCheckpointStore {
    return CachedRegionalRateCardCheckpointStore(
        inner = inner,
        cache = newTtlCache<RegionalRateCardCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional rate card checkpoints. Empty fields match all. */
data class RegionalRateCardCheckpointQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRateCardCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRateCardCheckpoint>): MutableList<RegionalRateCardCheckpoint> {
        val matched = filter(items) { candidate: RegionalRateCardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRateCardCheckpoints(store: RegionalRateCardCheckpointStore, query: RegionalRateCardCheckpointQuery): MutableList<RegionalRateCardCheckpoint> {
    return query.apply(store.list())
}
