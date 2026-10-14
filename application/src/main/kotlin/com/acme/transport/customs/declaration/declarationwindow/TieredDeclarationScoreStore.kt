package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered declaration scores by code. */
interface TieredDeclarationScoreStore {
    fun find(code: String): TieredDeclarationScore?
    fun save(item: TieredDeclarationScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDeclarationScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDeclarationScoreStore(
    var items: MutableMap<String, TieredDeclarationScore>,
    var order: MutableList<String>,
) : TieredDeclarationScoreStore {
    override fun find(code: String): TieredDeclarationScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDeclarationScore) {
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

    override fun list(): MutableList<TieredDeclarationScore> {
        val out = mutableListOf<TieredDeclarationScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDeclarationScoreStore(): TieredDeclarationScoreStore {
    return MemoryTieredDeclarationScoreStore(
        mutableMapOf<String, TieredDeclarationScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDeclarationScoreStore(
    var inner: TieredDeclarationScoreStore,
    var cache: TtlCache<TieredDeclarationScore>,
    var hits: Int,
    var misses: Int,
) : TieredDeclarationScoreStore {
    override fun find(code: String): TieredDeclarationScore? {
        val hit = (cache.get(code) as TieredDeclarationScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDeclarationScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDeclarationScore> = inner.list()
}

fun newCachedTieredDeclarationScoreStore(inner: TieredDeclarationScoreStore, ttl: Int, clock: Clock): TieredDeclarationScoreStore {
    return CachedTieredDeclarationScoreStore(
        inner = inner,
        cache = newTtlCache<TieredDeclarationScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered declaration scores. Empty fields match all. */
data class TieredDeclarationScoreQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDeclarationScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDeclarationScore>): MutableList<TieredDeclarationScore> {
        val matched = filter(items) { candidate: TieredDeclarationScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDeclarationScores(store: TieredDeclarationScoreStore, query: TieredDeclarationScoreQuery): MutableList<TieredDeclarationScore> {
    return query.apply(store.list())
}
