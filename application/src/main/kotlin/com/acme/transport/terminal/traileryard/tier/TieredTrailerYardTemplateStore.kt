package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered trailer yard templates by code. */
interface TieredTrailerYardTemplateStore {
    fun find(code: String): TieredTrailerYardTemplate?
    fun save(item: TieredTrailerYardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTrailerYardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTrailerYardTemplateStore(
    var items: MutableMap<String, TieredTrailerYardTemplate>,
    var order: MutableList<String>,
) : TieredTrailerYardTemplateStore {
    override fun find(code: String): TieredTrailerYardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTrailerYardTemplate) {
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

    override fun list(): MutableList<TieredTrailerYardTemplate> {
        val out = mutableListOf<TieredTrailerYardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTrailerYardTemplateStore(): TieredTrailerYardTemplateStore {
    return MemoryTieredTrailerYardTemplateStore(
        mutableMapOf<String, TieredTrailerYardTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTrailerYardTemplateStore(
    var inner: TieredTrailerYardTemplateStore,
    var cache: TtlCache<TieredTrailerYardTemplate>,
    var hits: Int,
    var misses: Int,
) : TieredTrailerYardTemplateStore {
    override fun find(code: String): TieredTrailerYardTemplate? {
        val hit = (cache.get(code) as TieredTrailerYardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTrailerYardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTrailerYardTemplate> = inner.list()
}

fun newCachedTieredTrailerYardTemplateStore(inner: TieredTrailerYardTemplateStore, ttl: Int, clock: Clock): TieredTrailerYardTemplateStore {
    return CachedTieredTrailerYardTemplateStore(
        inner = inner,
        cache = newTtlCache<TieredTrailerYardTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered trailer yard templates. Empty fields match all. */
data class TieredTrailerYardTemplateQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTrailerYardTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTrailerYardTemplate>): MutableList<TieredTrailerYardTemplate> {
        val matched = filter(items) { candidate: TieredTrailerYardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTrailerYardTemplates(store: TieredTrailerYardTemplateStore, query: TieredTrailerYardTemplateQuery): MutableList<TieredTrailerYardTemplate> {
    return query.apply(store.list())
}
