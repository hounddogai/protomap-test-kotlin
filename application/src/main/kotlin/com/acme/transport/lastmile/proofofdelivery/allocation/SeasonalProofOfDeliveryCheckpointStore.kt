package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal proof of delivery checkpoints by code. */
interface SeasonalProofOfDeliveryCheckpointStore {
    fun find(code: String): SeasonalProofOfDeliveryCheckpoint?
    fun save(item: SeasonalProofOfDeliveryCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalProofOfDeliveryCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalProofOfDeliveryCheckpointStore(
    var items: MutableMap<String, SeasonalProofOfDeliveryCheckpoint>,
    var order: MutableList<String>,
) : SeasonalProofOfDeliveryCheckpointStore {
    override fun find(code: String): SeasonalProofOfDeliveryCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalProofOfDeliveryCheckpoint) {
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

    override fun list(): MutableList<SeasonalProofOfDeliveryCheckpoint> {
        val out = mutableListOf<SeasonalProofOfDeliveryCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalProofOfDeliveryCheckpointStore(): SeasonalProofOfDeliveryCheckpointStore {
    return MemorySeasonalProofOfDeliveryCheckpointStore(
        mutableMapOf<String, SeasonalProofOfDeliveryCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalProofOfDeliveryCheckpointStore(
    var inner: SeasonalProofOfDeliveryCheckpointStore,
    var cache: TtlCache<SeasonalProofOfDeliveryCheckpoint>,
    var hits: Int,
    var misses: Int,
) : SeasonalProofOfDeliveryCheckpointStore {
    override fun find(code: String): SeasonalProofOfDeliveryCheckpoint? {
        val hit = (cache.get(code) as SeasonalProofOfDeliveryCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalProofOfDeliveryCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalProofOfDeliveryCheckpoint> = inner.list()
}

fun newCachedSeasonalProofOfDeliveryCheckpointStore(inner: SeasonalProofOfDeliveryCheckpointStore, ttl: Int, clock: Clock): SeasonalProofOfDeliveryCheckpointStore {
    return CachedSeasonalProofOfDeliveryCheckpointStore(
        inner = inner,
        cache = newTtlCache<SeasonalProofOfDeliveryCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal proof of delivery checkpoints. Empty fields match all. */
data class SeasonalProofOfDeliveryCheckpointQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalProofOfDeliveryCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalProofOfDeliveryCheckpoint>): MutableList<SeasonalProofOfDeliveryCheckpoint> {
        val matched = filter(items) { candidate: SeasonalProofOfDeliveryCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalProofOfDeliveryCheckpoints(store: SeasonalProofOfDeliveryCheckpointStore, query: SeasonalProofOfDeliveryCheckpointQuery): MutableList<SeasonalProofOfDeliveryCheckpoint> {
    return query.apply(store.list())
}
