package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling tariff checkpoints by code. */
interface RollingTariffCheckpointStore {
    fun find(code: String): RollingTariffCheckpoint?
    fun save(item: RollingTariffCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTariffCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTariffCheckpointStore(
    var items: MutableMap<String, RollingTariffCheckpoint>,
    var order: MutableList<String>,
) : RollingTariffCheckpointStore {
    override fun find(code: String): RollingTariffCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTariffCheckpoint) {
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

    override fun list(): MutableList<RollingTariffCheckpoint> {
        val out = mutableListOf<RollingTariffCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTariffCheckpointStore(): RollingTariffCheckpointStore {
    return MemoryRollingTariffCheckpointStore(
        mutableMapOf<String, RollingTariffCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTariffCheckpointStore(
    var inner: RollingTariffCheckpointStore,
    var cache: TtlCache<RollingTariffCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RollingTariffCheckpointStore {
    override fun find(code: String): RollingTariffCheckpoint? {
        val hit = (cache.get(code) as RollingTariffCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTariffCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTariffCheckpoint> = inner.list()
}

fun newCachedRollingTariffCheckpointStore(inner: RollingTariffCheckpointStore, ttl: Int, clock: Clock): RollingTariffCheckpointStore {
    return CachedRollingTariffCheckpointStore(
        inner = inner,
        cache = newTtlCache<RollingTariffCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tariff checkpoints. Empty fields match all. */
data class RollingTariffCheckpointQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTariffCheckpoint): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTariffCheckpoint>): MutableList<RollingTariffCheckpoint> {
        val matched = filter(items) { candidate: RollingTariffCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTariffCheckpoints(store: RollingTariffCheckpointStore, query: RollingTariffCheckpointQuery): MutableList<RollingTariffCheckpoint> {
    return query.apply(store.list())
}
