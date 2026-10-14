package com.acme.transport.network.node.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal node templates by code. */
interface SeasonalNodeTemplateStore {
    fun find(code: String): SeasonalNodeTemplate?
    fun save(item: SeasonalNodeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalNodeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalNodeTemplateStore(
    var items: MutableMap<String, SeasonalNodeTemplate>,
    var order: MutableList<String>,
) : SeasonalNodeTemplateStore {
    override fun find(code: String): SeasonalNodeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalNodeTemplate) {
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

    override fun list(): MutableList<SeasonalNodeTemplate> {
        val out = mutableListOf<SeasonalNodeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalNodeTemplateStore(): SeasonalNodeTemplateStore {
    return MemorySeasonalNodeTemplateStore(
        mutableMapOf<String, SeasonalNodeTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalNodeTemplateStore(
    var inner: SeasonalNodeTemplateStore,
    var cache: TtlCache<SeasonalNodeTemplate>,
    var hits: Int,
    var misses: Int,
) : SeasonalNodeTemplateStore {
    override fun find(code: String): SeasonalNodeTemplate? {
        val hit = (cache.get(code) as SeasonalNodeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalNodeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalNodeTemplate> = inner.list()
}

fun newCachedSeasonalNodeTemplateStore(inner: SeasonalNodeTemplateStore, ttl: Int, clock: Clock): SeasonalNodeTemplateStore {
    return CachedSeasonalNodeTemplateStore(
        inner = inner,
        cache = newTtlCache<SeasonalNodeTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal node templates. Empty fields match all. */
data class SeasonalNodeTemplateQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalNodeTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalNodeTemplate>): MutableList<SeasonalNodeTemplate> {
        val matched = filter(items) { candidate: SeasonalNodeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalNodeTemplates(store: SeasonalNodeTemplateStore, query: SeasonalNodeTemplateQuery): MutableList<SeasonalNodeTemplate> {
    return query.apply(store.list())
}
