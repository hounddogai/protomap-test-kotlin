package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff checkpoints by code. */
interface TariffCheckpointStore {
    fun find(code: String): TariffCheckpoint?
    fun save(item: TariffCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffCheckpointStore(
    var items: MutableMap<String, TariffCheckpoint>,
    var order: MutableList<String>,
) : TariffCheckpointStore {
    override fun find(code: String): TariffCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffCheckpoint) {
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

    override fun list(): MutableList<TariffCheckpoint> {
        val out = mutableListOf<TariffCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffCheckpointStore(): TariffCheckpointStore {
    return MemoryTariffCheckpointStore(mutableMapOf<String, TariffCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffCheckpointStore(
    var inner: TariffCheckpointStore,
    var cache: TtlCache<TariffCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TariffCheckpointStore {
    override fun find(code: String): TariffCheckpoint? {
        val hit = (cache.get(code) as TariffCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffCheckpoint> = inner.list()
}

fun newCachedTariffCheckpointStore(inner: TariffCheckpointStore, ttl: Int, clock: Clock): TariffCheckpointStore {
    return CachedTariffCheckpointStore(
        inner = inner,
        cache = newTtlCache<TariffCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff checkpoints. Empty fields match all. */
data class TariffCheckpointQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffCheckpoint): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TariffCheckpoint>): MutableList<TariffCheckpoint> {
        val matched = filter(items) { candidate: TariffCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffCheckpoints(store: TariffCheckpointStore, query: TariffCheckpointQuery): MutableList<TariffCheckpoint> {
    return query.apply(store.list())
}
