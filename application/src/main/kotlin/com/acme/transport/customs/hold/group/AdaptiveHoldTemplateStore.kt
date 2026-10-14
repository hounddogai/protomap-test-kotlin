package com.acme.transport.customs.hold.group

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive hold templates by code. */
interface AdaptiveHoldTemplateStore {
    fun find(code: String): AdaptiveHoldTemplate?
    fun save(item: AdaptiveHoldTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveHoldTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveHoldTemplateStore(
    var items: MutableMap<String, AdaptiveHoldTemplate>,
    var order: MutableList<String>,
) : AdaptiveHoldTemplateStore {
    override fun find(code: String): AdaptiveHoldTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveHoldTemplate) {
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

    override fun list(): MutableList<AdaptiveHoldTemplate> {
        val out = mutableListOf<AdaptiveHoldTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveHoldTemplateStore(): AdaptiveHoldTemplateStore {
    return MemoryAdaptiveHoldTemplateStore(
        mutableMapOf<String, AdaptiveHoldTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveHoldTemplateStore(
    var inner: AdaptiveHoldTemplateStore,
    var cache: TtlCache<AdaptiveHoldTemplate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveHoldTemplateStore {
    override fun find(code: String): AdaptiveHoldTemplate? {
        val hit = (cache.get(code) as AdaptiveHoldTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveHoldTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveHoldTemplate> = inner.list()
}

fun newCachedAdaptiveHoldTemplateStore(inner: AdaptiveHoldTemplateStore, ttl: Int, clock: Clock): AdaptiveHoldTemplateStore {
    return CachedAdaptiveHoldTemplateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveHoldTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive hold templates. Empty fields match all. */
data class AdaptiveHoldTemplateQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveHoldTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveHoldTemplate>): MutableList<AdaptiveHoldTemplate> {
        val matched = filter(items) { candidate: AdaptiveHoldTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveHoldTemplates(store: AdaptiveHoldTemplateStore, query: AdaptiveHoldTemplateQuery): MutableList<AdaptiveHoldTemplate> {
    return query.apply(store.list())
}
