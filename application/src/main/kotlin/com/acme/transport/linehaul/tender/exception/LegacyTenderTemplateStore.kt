package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy tender templates by code. */
interface LegacyTenderTemplateStore {
    fun find(code: String): LegacyTenderTemplate?
    fun save(item: LegacyTenderTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTenderTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTenderTemplateStore(
    var items: MutableMap<String, LegacyTenderTemplate>,
    var order: MutableList<String>,
) : LegacyTenderTemplateStore {
    override fun find(code: String): LegacyTenderTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTenderTemplate) {
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

    override fun list(): MutableList<LegacyTenderTemplate> {
        val out = mutableListOf<LegacyTenderTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTenderTemplateStore(): LegacyTenderTemplateStore {
    return MemoryLegacyTenderTemplateStore(
        mutableMapOf<String, LegacyTenderTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTenderTemplateStore(
    var inner: LegacyTenderTemplateStore,
    var cache: TtlCache<LegacyTenderTemplate>,
    var hits: Int,
    var misses: Int,
) : LegacyTenderTemplateStore {
    override fun find(code: String): LegacyTenderTemplate? {
        val hit = (cache.get(code) as LegacyTenderTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTenderTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTenderTemplate> = inner.list()
}

fun newCachedLegacyTenderTemplateStore(inner: LegacyTenderTemplateStore, ttl: Int, clock: Clock): LegacyTenderTemplateStore {
    return CachedLegacyTenderTemplateStore(
        inner = inner,
        cache = newTtlCache<LegacyTenderTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tender templates. Empty fields match all. */
data class LegacyTenderTemplateQuery(
    var segment: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTenderTemplate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<LegacyTenderTemplate>): MutableList<LegacyTenderTemplate> {
        val matched = filter(items) { candidate: LegacyTenderTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTenderTemplates(store: LegacyTenderTemplateStore, query: LegacyTenderTemplateQuery): MutableList<LegacyTenderTemplate> {
    return query.apply(store.list())
}
