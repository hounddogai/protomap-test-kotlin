package com.acme.transport.customs.declaration.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration checkpoints by code. */
interface DeclarationCheckpointStore {
    fun find(code: String): DeclarationCheckpoint?
    fun save(item: DeclarationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationCheckpointStore(
    var items: MutableMap<String, DeclarationCheckpoint>,
    var order: MutableList<String>,
) : DeclarationCheckpointStore {
    override fun find(code: String): DeclarationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationCheckpoint) {
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

    override fun list(): MutableList<DeclarationCheckpoint> {
        val out = mutableListOf<DeclarationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationCheckpointStore(): DeclarationCheckpointStore {
    return MemoryDeclarationCheckpointStore(
        mutableMapOf<String, DeclarationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationCheckpointStore(
    var inner: DeclarationCheckpointStore,
    var cache: TtlCache<DeclarationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DeclarationCheckpointStore {
    override fun find(code: String): DeclarationCheckpoint? {
        val hit = (cache.get(code) as DeclarationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationCheckpoint> = inner.list()
}

fun newCachedDeclarationCheckpointStore(inner: DeclarationCheckpointStore, ttl: Int, clock: Clock): DeclarationCheckpointStore {
    return CachedDeclarationCheckpointStore(
        inner = inner,
        cache = newTtlCache<DeclarationCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration checkpoints. Empty fields match all. */
data class DeclarationCheckpointQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationCheckpoint>): MutableList<DeclarationCheckpoint> {
        val matched = filter(items) { candidate: DeclarationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationCheckpoints(store: DeclarationCheckpointStore, query: DeclarationCheckpointQuery): MutableList<DeclarationCheckpoint> {
    return query.apply(store.list())
}
