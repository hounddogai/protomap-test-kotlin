package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered drop templates by code. */
interface TieredDropTemplateStore {
    fun find(code: String): TieredDropTemplate?
    fun save(item: TieredDropTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDropTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDropTemplateStore(
    var items: MutableMap<String, TieredDropTemplate>,
    var order: MutableList<String>,
) : TieredDropTemplateStore {
    override fun find(code: String): TieredDropTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDropTemplate) {
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

    override fun list(): MutableList<TieredDropTemplate> {
        val out = mutableListOf<TieredDropTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDropTemplateStore(): TieredDropTemplateStore {
    return MemoryTieredDropTemplateStore(mutableMapOf<String, TieredDropTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDropTemplateStore(
    var inner: TieredDropTemplateStore,
    var cache: TtlCache<TieredDropTemplate>,
    var hits: Int,
    var misses: Int,
) : TieredDropTemplateStore {
    override fun find(code: String): TieredDropTemplate? {
        val hit = (cache.get(code) as TieredDropTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDropTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDropTemplate> = inner.list()
}

fun newCachedTieredDropTemplateStore(inner: TieredDropTemplateStore, ttl: Int, clock: Clock): TieredDropTemplateStore {
    return CachedTieredDropTemplateStore(
        inner = inner,
        cache = newTtlCache<TieredDropTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered drop templates. Empty fields match all. */
data class TieredDropTemplateQuery(
    var locale: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDropTemplate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDropTemplate>): MutableList<TieredDropTemplate> {
        val matched = filter(items) { candidate: TieredDropTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDropTemplates(store: TieredDropTemplateStore, query: TieredDropTemplateQuery): MutableList<TieredDropTemplate> {
    return query.apply(store.list())
}
