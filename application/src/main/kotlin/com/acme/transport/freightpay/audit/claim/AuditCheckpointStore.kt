package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit checkpoints by code. */
interface AuditCheckpointStore {
    fun find(code: String): AuditCheckpoint?
    fun save(item: AuditCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditCheckpointStore(
    var items: MutableMap<String, AuditCheckpoint>,
    var order: MutableList<String>,
) : AuditCheckpointStore {
    override fun find(code: String): AuditCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditCheckpoint) {
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

    override fun list(): MutableList<AuditCheckpoint> {
        val out = mutableListOf<AuditCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditCheckpointStore(): AuditCheckpointStore {
    return MemoryAuditCheckpointStore(mutableMapOf<String, AuditCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditCheckpointStore(
    var inner: AuditCheckpointStore,
    var cache: TtlCache<AuditCheckpoint>,
    var hits: Int,
    var misses: Int,
) : AuditCheckpointStore {
    override fun find(code: String): AuditCheckpoint? {
        val hit = (cache.get(code) as AuditCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditCheckpoint> = inner.list()
}

fun newCachedAuditCheckpointStore(inner: AuditCheckpointStore, ttl: Int, clock: Clock): AuditCheckpointStore {
    return CachedAuditCheckpointStore(
        inner = inner,
        cache = newTtlCache<AuditCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit checkpoints. Empty fields match all. */
data class AuditCheckpointQuery(
    var segment: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditCheckpoint): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<AuditCheckpoint>): MutableList<AuditCheckpoint> {
        val matched = filter(items) { candidate: AuditCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditCheckpoints(store: AuditCheckpointStore, query: AuditCheckpointQuery): MutableList<AuditCheckpoint> {
    return query.apply(store.list())
}
