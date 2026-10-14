package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled hold templates by code. */
interface ScheduledHoldTemplateStore {
    fun find(code: String): ScheduledHoldTemplate?
    fun save(item: ScheduledHoldTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHoldTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHoldTemplateStore(
    var items: MutableMap<String, ScheduledHoldTemplate>,
    var order: MutableList<String>,
) : ScheduledHoldTemplateStore {
    override fun find(code: String): ScheduledHoldTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHoldTemplate) {
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

    override fun list(): MutableList<ScheduledHoldTemplate> {
        val out = mutableListOf<ScheduledHoldTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHoldTemplateStore(): ScheduledHoldTemplateStore {
    return MemoryScheduledHoldTemplateStore(
        mutableMapOf<String, ScheduledHoldTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHoldTemplateStore(
    var inner: ScheduledHoldTemplateStore,
    var cache: TtlCache<ScheduledHoldTemplate>,
    var hits: Int,
    var misses: Int,
) : ScheduledHoldTemplateStore {
    override fun find(code: String): ScheduledHoldTemplate? {
        val hit = (cache.get(code) as ScheduledHoldTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHoldTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHoldTemplate> = inner.list()
}

fun newCachedScheduledHoldTemplateStore(inner: ScheduledHoldTemplateStore, ttl: Int, clock: Clock): ScheduledHoldTemplateStore {
    return CachedScheduledHoldTemplateStore(
        inner = inner,
        cache = newTtlCache<ScheduledHoldTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hold templates. Empty fields match all. */
data class ScheduledHoldTemplateQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHoldTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHoldTemplate>): MutableList<ScheduledHoldTemplate> {
        val matched = filter(items) { candidate: ScheduledHoldTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHoldTemplates(store: ScheduledHoldTemplateStore, query: ScheduledHoldTemplateQuery): MutableList<ScheduledHoldTemplate> {
    return query.apply(store.list())
}
