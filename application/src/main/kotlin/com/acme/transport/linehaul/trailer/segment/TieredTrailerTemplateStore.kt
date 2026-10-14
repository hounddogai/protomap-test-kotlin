package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered trailer templates by code. */
interface TieredTrailerTemplateStore {
    fun find(code: String): TieredTrailerTemplate?
    fun save(item: TieredTrailerTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTrailerTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTrailerTemplateStore(
    var items: MutableMap<String, TieredTrailerTemplate>,
    var order: MutableList<String>,
) : TieredTrailerTemplateStore {
    override fun find(code: String): TieredTrailerTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTrailerTemplate) {
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

    override fun list(): MutableList<TieredTrailerTemplate> {
        val out = mutableListOf<TieredTrailerTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTrailerTemplateStore(): TieredTrailerTemplateStore {
    return MemoryTieredTrailerTemplateStore(
        mutableMapOf<String, TieredTrailerTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTrailerTemplateStore(
    var inner: TieredTrailerTemplateStore,
    var cache: TtlCache<TieredTrailerTemplate>,
    var hits: Int,
    var misses: Int,
) : TieredTrailerTemplateStore {
    override fun find(code: String): TieredTrailerTemplate? {
        val hit = (cache.get(code) as TieredTrailerTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTrailerTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTrailerTemplate> = inner.list()
}

fun newCachedTieredTrailerTemplateStore(inner: TieredTrailerTemplateStore, ttl: Int, clock: Clock): TieredTrailerTemplateStore {
    return CachedTieredTrailerTemplateStore(
        inner = inner,
        cache = newTtlCache<TieredTrailerTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered trailer templates. Empty fields match all. */
data class TieredTrailerTemplateQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTrailerTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTrailerTemplate>): MutableList<TieredTrailerTemplate> {
        val matched = filter(items) { candidate: TieredTrailerTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTrailerTemplates(store: TieredTrailerTemplateStore, query: TieredTrailerTemplateQuery): MutableList<TieredTrailerTemplate> {
    return query.apply(store.list())
}
