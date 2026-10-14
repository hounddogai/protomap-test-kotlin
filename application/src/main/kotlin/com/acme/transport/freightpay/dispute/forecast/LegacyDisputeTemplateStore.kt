package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy dispute templates by code. */
interface LegacyDisputeTemplateStore {
    fun find(code: String): LegacyDisputeTemplate?
    fun save(item: LegacyDisputeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDisputeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDisputeTemplateStore(
    var items: MutableMap<String, LegacyDisputeTemplate>,
    var order: MutableList<String>,
) : LegacyDisputeTemplateStore {
    override fun find(code: String): LegacyDisputeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDisputeTemplate) {
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

    override fun list(): MutableList<LegacyDisputeTemplate> {
        val out = mutableListOf<LegacyDisputeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDisputeTemplateStore(): LegacyDisputeTemplateStore {
    return MemoryLegacyDisputeTemplateStore(
        mutableMapOf<String, LegacyDisputeTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDisputeTemplateStore(
    var inner: LegacyDisputeTemplateStore,
    var cache: TtlCache<LegacyDisputeTemplate>,
    var hits: Int,
    var misses: Int,
) : LegacyDisputeTemplateStore {
    override fun find(code: String): LegacyDisputeTemplate? {
        val hit = (cache.get(code) as LegacyDisputeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDisputeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDisputeTemplate> = inner.list()
}

fun newCachedLegacyDisputeTemplateStore(inner: LegacyDisputeTemplateStore, ttl: Int, clock: Clock): LegacyDisputeTemplateStore {
    return CachedLegacyDisputeTemplateStore(
        inner = inner,
        cache = newTtlCache<LegacyDisputeTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy dispute templates. Empty fields match all. */
data class LegacyDisputeTemplateQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDisputeTemplate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDisputeTemplate>): MutableList<LegacyDisputeTemplate> {
        val matched = filter(items) { candidate: LegacyDisputeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDisputeTemplates(store: LegacyDisputeTemplateStore, query: LegacyDisputeTemplateQuery): MutableList<LegacyDisputeTemplate> {
    return query.apply(store.list())
}
