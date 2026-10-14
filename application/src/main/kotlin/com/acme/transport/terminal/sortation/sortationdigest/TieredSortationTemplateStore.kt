package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered sortation templates by code. */
interface TieredSortationTemplateStore {
    fun find(code: String): TieredSortationTemplate?
    fun save(item: TieredSortationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSortationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSortationTemplateStore(
    var items: MutableMap<String, TieredSortationTemplate>,
    var order: MutableList<String>,
) : TieredSortationTemplateStore {
    override fun find(code: String): TieredSortationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSortationTemplate) {
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

    override fun list(): MutableList<TieredSortationTemplate> {
        val out = mutableListOf<TieredSortationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSortationTemplateStore(): TieredSortationTemplateStore {
    return MemoryTieredSortationTemplateStore(
        mutableMapOf<String, TieredSortationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSortationTemplateStore(
    var inner: TieredSortationTemplateStore,
    var cache: TtlCache<TieredSortationTemplate>,
    var hits: Int,
    var misses: Int,
) : TieredSortationTemplateStore {
    override fun find(code: String): TieredSortationTemplate? {
        val hit = (cache.get(code) as TieredSortationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSortationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSortationTemplate> = inner.list()
}

fun newCachedTieredSortationTemplateStore(inner: TieredSortationTemplateStore, ttl: Int, clock: Clock): TieredSortationTemplateStore {
    return CachedTieredSortationTemplateStore(
        inner = inner,
        cache = newTtlCache<TieredSortationTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered sortation templates. Empty fields match all. */
data class TieredSortationTemplateQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSortationTemplate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredSortationTemplate>): MutableList<TieredSortationTemplate> {
        val matched = filter(items) { candidate: TieredSortationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSortationTemplates(store: TieredSortationTemplateStore, query: TieredSortationTemplateQuery): MutableList<TieredSortationTemplate> {
    return query.apply(store.list())
}
