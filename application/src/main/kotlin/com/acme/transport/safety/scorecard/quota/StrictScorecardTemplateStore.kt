package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict scorecard templates by code. */
interface StrictScorecardTemplateStore {
    fun find(code: String): StrictScorecardTemplate?
    fun save(item: StrictScorecardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictScorecardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictScorecardTemplateStore(
    var items: MutableMap<String, StrictScorecardTemplate>,
    var order: MutableList<String>,
) : StrictScorecardTemplateStore {
    override fun find(code: String): StrictScorecardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictScorecardTemplate) {
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

    override fun list(): MutableList<StrictScorecardTemplate> {
        val out = mutableListOf<StrictScorecardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictScorecardTemplateStore(): StrictScorecardTemplateStore {
    return MemoryStrictScorecardTemplateStore(
        mutableMapOf<String, StrictScorecardTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictScorecardTemplateStore(
    var inner: StrictScorecardTemplateStore,
    var cache: TtlCache<StrictScorecardTemplate>,
    var hits: Int,
    var misses: Int,
) : StrictScorecardTemplateStore {
    override fun find(code: String): StrictScorecardTemplate? {
        val hit = (cache.get(code) as StrictScorecardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictScorecardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictScorecardTemplate> = inner.list()
}

fun newCachedStrictScorecardTemplateStore(inner: StrictScorecardTemplateStore, ttl: Int, clock: Clock): StrictScorecardTemplateStore {
    return CachedStrictScorecardTemplateStore(
        inner = inner,
        cache = newTtlCache<StrictScorecardTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict scorecard templates. Empty fields match all. */
data class StrictScorecardTemplateQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictScorecardTemplate): Boolean {
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

    fun apply(items: MutableList<StrictScorecardTemplate>): MutableList<StrictScorecardTemplate> {
        val matched = filter(items) { candidate: StrictScorecardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictScorecardTemplates(store: StrictScorecardTemplateStore, query: StrictScorecardTemplateQuery): MutableList<StrictScorecardTemplate> {
    return query.apply(store.list())
}
