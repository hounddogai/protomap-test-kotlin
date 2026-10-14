package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped accessorial checkpoints by code. */
interface CappedAccessorialCheckpointStore {
    fun find(code: String): CappedAccessorialCheckpoint?
    fun save(item: CappedAccessorialCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedAccessorialCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedAccessorialCheckpointStore(
    var items: MutableMap<String, CappedAccessorialCheckpoint>,
    var order: MutableList<String>,
) : CappedAccessorialCheckpointStore {
    override fun find(code: String): CappedAccessorialCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedAccessorialCheckpoint) {
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

    override fun list(): MutableList<CappedAccessorialCheckpoint> {
        val out = mutableListOf<CappedAccessorialCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedAccessorialCheckpointStore(): CappedAccessorialCheckpointStore {
    return MemoryCappedAccessorialCheckpointStore(
        mutableMapOf<String, CappedAccessorialCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedAccessorialCheckpointStore(
    var inner: CappedAccessorialCheckpointStore,
    var cache: TtlCache<CappedAccessorialCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CappedAccessorialCheckpointStore {
    override fun find(code: String): CappedAccessorialCheckpoint? {
        val hit = (cache.get(code) as CappedAccessorialCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedAccessorialCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedAccessorialCheckpoint> = inner.list()
}

fun newCachedCappedAccessorialCheckpointStore(inner: CappedAccessorialCheckpointStore, ttl: Int, clock: Clock): CappedAccessorialCheckpointStore {
    return CachedCappedAccessorialCheckpointStore(
        inner = inner,
        cache = newTtlCache<CappedAccessorialCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped accessorial checkpoints. Empty fields match all. */
data class CappedAccessorialCheckpointQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedAccessorialCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedAccessorialCheckpoint>): MutableList<CappedAccessorialCheckpoint> {
        val matched = filter(items) { candidate: CappedAccessorialCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedAccessorialCheckpoints(store: CappedAccessorialCheckpointStore, query: CappedAccessorialCheckpointQuery): MutableList<CappedAccessorialCheckpoint> {
    return query.apply(store.list())
}
