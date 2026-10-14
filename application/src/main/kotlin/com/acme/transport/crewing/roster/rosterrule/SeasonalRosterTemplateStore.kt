package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal roster templates by code. */
interface SeasonalRosterTemplateStore {
    fun find(code: String): SeasonalRosterTemplate?
    fun save(item: SeasonalRosterTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRosterTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRosterTemplateStore(
    var items: MutableMap<String, SeasonalRosterTemplate>,
    var order: MutableList<String>,
) : SeasonalRosterTemplateStore {
    override fun find(code: String): SeasonalRosterTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRosterTemplate) {
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

    override fun list(): MutableList<SeasonalRosterTemplate> {
        val out = mutableListOf<SeasonalRosterTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRosterTemplateStore(): SeasonalRosterTemplateStore {
    return MemorySeasonalRosterTemplateStore(
        mutableMapOf<String, SeasonalRosterTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRosterTemplateStore(
    var inner: SeasonalRosterTemplateStore,
    var cache: TtlCache<SeasonalRosterTemplate>,
    var hits: Int,
    var misses: Int,
) : SeasonalRosterTemplateStore {
    override fun find(code: String): SeasonalRosterTemplate? {
        val hit = (cache.get(code) as SeasonalRosterTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRosterTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRosterTemplate> = inner.list()
}

fun newCachedSeasonalRosterTemplateStore(inner: SeasonalRosterTemplateStore, ttl: Int, clock: Clock): SeasonalRosterTemplateStore {
    return CachedSeasonalRosterTemplateStore(
        inner = inner,
        cache = newTtlCache<SeasonalRosterTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal roster templates. Empty fields match all. */
data class SeasonalRosterTemplateQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRosterTemplate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRosterTemplate>): MutableList<SeasonalRosterTemplate> {
        val matched = filter(items) { candidate: SeasonalRosterTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRosterTemplates(store: SeasonalRosterTemplateStore, query: SeasonalRosterTemplateQuery): MutableList<SeasonalRosterTemplate> {
    return query.apply(store.list())
}
