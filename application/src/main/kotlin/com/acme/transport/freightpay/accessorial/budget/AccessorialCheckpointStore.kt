package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial checkpoints by code. */
interface AccessorialCheckpointStore {
    fun find(code: String): AccessorialCheckpoint?
    fun save(item: AccessorialCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialCheckpointStore(
    var items: MutableMap<String, AccessorialCheckpoint>,
    var order: MutableList<String>,
) : AccessorialCheckpointStore {
    override fun find(code: String): AccessorialCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialCheckpoint) {
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

    override fun list(): MutableList<AccessorialCheckpoint> {
        val out = mutableListOf<AccessorialCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialCheckpointStore(): AccessorialCheckpointStore {
    return MemoryAccessorialCheckpointStore(
        mutableMapOf<String, AccessorialCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialCheckpointStore(
    var inner: AccessorialCheckpointStore,
    var cache: TtlCache<AccessorialCheckpoint>,
    var hits: Int,
    var misses: Int,
) : AccessorialCheckpointStore {
    override fun find(code: String): AccessorialCheckpoint? {
        val hit = (cache.get(code) as AccessorialCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialCheckpoint> = inner.list()
}

fun newCachedAccessorialCheckpointStore(inner: AccessorialCheckpointStore, ttl: Int, clock: Clock): AccessorialCheckpointStore {
    return CachedAccessorialCheckpointStore(
        inner = inner,
        cache = newTtlCache<AccessorialCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial checkpoints. Empty fields match all. */
data class AccessorialCheckpointQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialCheckpoint>): MutableList<AccessorialCheckpoint> {
        val matched = filter(items) { candidate: AccessorialCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialCheckpoints(store: AccessorialCheckpointStore, query: AccessorialCheckpointQuery): MutableList<AccessorialCheckpoint> {
    return query.apply(store.list())
}
