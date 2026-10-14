package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification checkpoints by code. */
interface QualificationCheckpointStore {
    fun find(code: String): QualificationCheckpoint?
    fun save(item: QualificationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationCheckpointStore(
    var items: MutableMap<String, QualificationCheckpoint>,
    var order: MutableList<String>,
) : QualificationCheckpointStore {
    override fun find(code: String): QualificationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationCheckpoint) {
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

    override fun list(): MutableList<QualificationCheckpoint> {
        val out = mutableListOf<QualificationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationCheckpointStore(): QualificationCheckpointStore {
    return MemoryQualificationCheckpointStore(
        mutableMapOf<String, QualificationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationCheckpointStore(
    var inner: QualificationCheckpointStore,
    var cache: TtlCache<QualificationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : QualificationCheckpointStore {
    override fun find(code: String): QualificationCheckpoint? {
        val hit = (cache.get(code) as QualificationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationCheckpoint> = inner.list()
}

fun newCachedQualificationCheckpointStore(inner: QualificationCheckpointStore, ttl: Int, clock: Clock): QualificationCheckpointStore {
    return CachedQualificationCheckpointStore(
        inner = inner,
        cache = newTtlCache<QualificationCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification checkpoints. Empty fields match all. */
data class QualificationCheckpointQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationCheckpoint): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<QualificationCheckpoint>): MutableList<QualificationCheckpoint> {
        val matched = filter(items) { candidate: QualificationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationCheckpoints(store: QualificationCheckpointStore, query: QualificationCheckpointQuery): MutableList<QualificationCheckpoint> {
    return query.apply(store.list())
}
