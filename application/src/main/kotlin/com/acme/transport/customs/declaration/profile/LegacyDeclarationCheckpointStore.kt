package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy declaration checkpoints by code. */
interface LegacyDeclarationCheckpointStore {
    fun find(code: String): LegacyDeclarationCheckpoint?
    fun save(item: LegacyDeclarationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDeclarationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDeclarationCheckpointStore(
    var items: MutableMap<String, LegacyDeclarationCheckpoint>,
    var order: MutableList<String>,
) : LegacyDeclarationCheckpointStore {
    override fun find(code: String): LegacyDeclarationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDeclarationCheckpoint) {
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

    override fun list(): MutableList<LegacyDeclarationCheckpoint> {
        val out = mutableListOf<LegacyDeclarationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDeclarationCheckpointStore(): LegacyDeclarationCheckpointStore {
    return MemoryLegacyDeclarationCheckpointStore(
        mutableMapOf<String, LegacyDeclarationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDeclarationCheckpointStore(
    var inner: LegacyDeclarationCheckpointStore,
    var cache: TtlCache<LegacyDeclarationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LegacyDeclarationCheckpointStore {
    override fun find(code: String): LegacyDeclarationCheckpoint? {
        val hit = (cache.get(code) as LegacyDeclarationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDeclarationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDeclarationCheckpoint> = inner.list()
}

fun newCachedLegacyDeclarationCheckpointStore(inner: LegacyDeclarationCheckpointStore, ttl: Int, clock: Clock): LegacyDeclarationCheckpointStore {
    return CachedLegacyDeclarationCheckpointStore(
        inner = inner,
        cache = newTtlCache<LegacyDeclarationCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy declaration checkpoints. Empty fields match all. */
data class LegacyDeclarationCheckpointQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDeclarationCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDeclarationCheckpoint>): MutableList<LegacyDeclarationCheckpoint> {
        val matched = filter(items) { candidate: LegacyDeclarationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDeclarationCheckpoints(store: LegacyDeclarationCheckpointStore, query: LegacyDeclarationCheckpointQuery): MutableList<LegacyDeclarationCheckpoint> {
    return query.apply(store.list())
}
