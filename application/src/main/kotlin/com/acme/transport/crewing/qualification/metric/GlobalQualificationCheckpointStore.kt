package com.acme.transport.crewing.qualification.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global qualification checkpoints by code. */
interface GlobalQualificationCheckpointStore {
    fun find(code: String): GlobalQualificationCheckpoint?
    fun save(item: GlobalQualificationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalQualificationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalQualificationCheckpointStore(
    var items: MutableMap<String, GlobalQualificationCheckpoint>,
    var order: MutableList<String>,
) : GlobalQualificationCheckpointStore {
    override fun find(code: String): GlobalQualificationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalQualificationCheckpoint) {
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

    override fun list(): MutableList<GlobalQualificationCheckpoint> {
        val out = mutableListOf<GlobalQualificationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalQualificationCheckpointStore(): GlobalQualificationCheckpointStore {
    return MemoryGlobalQualificationCheckpointStore(
        mutableMapOf<String, GlobalQualificationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalQualificationCheckpointStore(
    var inner: GlobalQualificationCheckpointStore,
    var cache: TtlCache<GlobalQualificationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : GlobalQualificationCheckpointStore {
    override fun find(code: String): GlobalQualificationCheckpoint? {
        val hit = (cache.get(code) as GlobalQualificationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalQualificationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalQualificationCheckpoint> = inner.list()
}

fun newCachedGlobalQualificationCheckpointStore(inner: GlobalQualificationCheckpointStore, ttl: Int, clock: Clock): GlobalQualificationCheckpointStore {
    return CachedGlobalQualificationCheckpointStore(
        inner = inner,
        cache = newTtlCache<GlobalQualificationCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global qualification checkpoints. Empty fields match all. */
data class GlobalQualificationCheckpointQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalQualificationCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<GlobalQualificationCheckpoint>): MutableList<GlobalQualificationCheckpoint> {
        val matched = filter(items) { candidate: GlobalQualificationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalQualificationCheckpoints(store: GlobalQualificationCheckpointStore, query: GlobalQualificationCheckpointQuery): MutableList<GlobalQualificationCheckpoint> {
    return query.apply(store.list())
}
