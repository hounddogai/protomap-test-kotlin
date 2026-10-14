package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual scorecard templates by code. */
interface ManualScorecardTemplateStore {
    fun find(code: String): ManualScorecardTemplate?
    fun save(item: ManualScorecardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualScorecardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualScorecardTemplateStore(
    var items: MutableMap<String, ManualScorecardTemplate>,
    var order: MutableList<String>,
) : ManualScorecardTemplateStore {
    override fun find(code: String): ManualScorecardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualScorecardTemplate) {
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

    override fun list(): MutableList<ManualScorecardTemplate> {
        val out = mutableListOf<ManualScorecardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualScorecardTemplateStore(): ManualScorecardTemplateStore {
    return MemoryManualScorecardTemplateStore(
        mutableMapOf<String, ManualScorecardTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualScorecardTemplateStore(
    var inner: ManualScorecardTemplateStore,
    var cache: TtlCache<ManualScorecardTemplate>,
    var hits: Int,
    var misses: Int,
) : ManualScorecardTemplateStore {
    override fun find(code: String): ManualScorecardTemplate? {
        val hit = (cache.get(code) as ManualScorecardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualScorecardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualScorecardTemplate> = inner.list()
}

fun newCachedManualScorecardTemplateStore(inner: ManualScorecardTemplateStore, ttl: Int, clock: Clock): ManualScorecardTemplateStore {
    return CachedManualScorecardTemplateStore(
        inner = inner,
        cache = newTtlCache<ManualScorecardTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual scorecard templates. Empty fields match all. */
data class ManualScorecardTemplateQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualScorecardTemplate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualScorecardTemplate>): MutableList<ManualScorecardTemplate> {
        val matched = filter(items) { candidate: ManualScorecardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualScorecardTemplates(store: ManualScorecardTemplateStore, query: ManualScorecardTemplateQuery): MutableList<ManualScorecardTemplate> {
    return query.apply(store.list())
}
