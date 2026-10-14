package com.acme.transport.customs.declaration.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered declaration baselines by code. */
interface TieredDeclarationBaselineStore {
    fun find(code: String): TieredDeclarationBaseline?
    fun save(item: TieredDeclarationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDeclarationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDeclarationBaselineStore(
    var items: MutableMap<String, TieredDeclarationBaseline>,
    var order: MutableList<String>,
) : TieredDeclarationBaselineStore {
    override fun find(code: String): TieredDeclarationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDeclarationBaseline) {
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

    override fun list(): MutableList<TieredDeclarationBaseline> {
        val out = mutableListOf<TieredDeclarationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDeclarationBaselineStore(): TieredDeclarationBaselineStore {
    return MemoryTieredDeclarationBaselineStore(
        mutableMapOf<String, TieredDeclarationBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDeclarationBaselineStore(
    var inner: TieredDeclarationBaselineStore,
    var cache: TtlCache<TieredDeclarationBaseline>,
    var hits: Int,
    var misses: Int,
) : TieredDeclarationBaselineStore {
    override fun find(code: String): TieredDeclarationBaseline? {
        val hit = (cache.get(code) as TieredDeclarationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDeclarationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDeclarationBaseline> = inner.list()
}

fun newCachedTieredDeclarationBaselineStore(inner: TieredDeclarationBaselineStore, ttl: Int, clock: Clock): TieredDeclarationBaselineStore {
    return CachedTieredDeclarationBaselineStore(
        inner = inner,
        cache = newTtlCache<TieredDeclarationBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered declaration baselines. Empty fields match all. */
data class TieredDeclarationBaselineQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDeclarationBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDeclarationBaseline>): MutableList<TieredDeclarationBaseline> {
        val matched = filter(items) { candidate: TieredDeclarationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDeclarationBaselines(store: TieredDeclarationBaselineStore, query: TieredDeclarationBaselineQuery): MutableList<TieredDeclarationBaseline> {
    return query.apply(store.list())
}
