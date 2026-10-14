package com.acme.transport.crewing.roster.template

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled roster templates by code. */
interface ScheduledRosterTemplateStore {
    fun find(code: String): ScheduledRosterTemplate?
    fun save(item: ScheduledRosterTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRosterTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRosterTemplateStore(
    var items: MutableMap<String, ScheduledRosterTemplate>,
    var order: MutableList<String>,
) : ScheduledRosterTemplateStore {
    override fun find(code: String): ScheduledRosterTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRosterTemplate) {
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

    override fun list(): MutableList<ScheduledRosterTemplate> {
        val out = mutableListOf<ScheduledRosterTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRosterTemplateStore(): ScheduledRosterTemplateStore {
    return MemoryScheduledRosterTemplateStore(
        mutableMapOf<String, ScheduledRosterTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRosterTemplateStore(
    var inner: ScheduledRosterTemplateStore,
    var cache: TtlCache<ScheduledRosterTemplate>,
    var hits: Int,
    var misses: Int,
) : ScheduledRosterTemplateStore {
    override fun find(code: String): ScheduledRosterTemplate? {
        val hit = (cache.get(code) as ScheduledRosterTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRosterTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRosterTemplate> = inner.list()
}

fun newCachedScheduledRosterTemplateStore(inner: ScheduledRosterTemplateStore, ttl: Int, clock: Clock): ScheduledRosterTemplateStore {
    return CachedScheduledRosterTemplateStore(
        inner = inner,
        cache = newTtlCache<ScheduledRosterTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled roster templates. Empty fields match all. */
data class ScheduledRosterTemplateQuery(
    var segment: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRosterTemplate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRosterTemplate>): MutableList<ScheduledRosterTemplate> {
        val matched = filter(items) { candidate: ScheduledRosterTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRosterTemplates(store: ScheduledRosterTemplateStore, query: ScheduledRosterTemplateQuery): MutableList<ScheduledRosterTemplate> {
    return query.apply(store.list())
}
