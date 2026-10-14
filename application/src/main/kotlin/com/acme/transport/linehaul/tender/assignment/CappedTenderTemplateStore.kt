package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped tender templates by code. */
interface CappedTenderTemplateStore {
    fun find(code: String): CappedTenderTemplate?
    fun save(item: CappedTenderTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTenderTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTenderTemplateStore(
    var items: MutableMap<String, CappedTenderTemplate>,
    var order: MutableList<String>,
) : CappedTenderTemplateStore {
    override fun find(code: String): CappedTenderTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTenderTemplate) {
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

    override fun list(): MutableList<CappedTenderTemplate> {
        val out = mutableListOf<CappedTenderTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTenderTemplateStore(): CappedTenderTemplateStore {
    return MemoryCappedTenderTemplateStore(
        mutableMapOf<String, CappedTenderTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTenderTemplateStore(
    var inner: CappedTenderTemplateStore,
    var cache: TtlCache<CappedTenderTemplate>,
    var hits: Int,
    var misses: Int,
) : CappedTenderTemplateStore {
    override fun find(code: String): CappedTenderTemplate? {
        val hit = (cache.get(code) as CappedTenderTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTenderTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTenderTemplate> = inner.list()
}

fun newCachedCappedTenderTemplateStore(inner: CappedTenderTemplateStore, ttl: Int, clock: Clock): CappedTenderTemplateStore {
    return CachedCappedTenderTemplateStore(
        inner = inner,
        cache = newTtlCache<CappedTenderTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tender templates. Empty fields match all. */
data class CappedTenderTemplateQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTenderTemplate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTenderTemplate>): MutableList<CappedTenderTemplate> {
        val matched = filter(items) { candidate: CappedTenderTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTenderTemplates(store: CappedTenderTemplateStore, query: CappedTenderTemplateQuery): MutableList<CappedTenderTemplate> {
    return query.apply(store.list())
}
