package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled redelivery templates by code. */
interface ScheduledRedeliveryTemplateStore {
    fun find(code: String): ScheduledRedeliveryTemplate?
    fun save(item: ScheduledRedeliveryTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRedeliveryTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRedeliveryTemplateStore(
    var items: MutableMap<String, ScheduledRedeliveryTemplate>,
    var order: MutableList<String>,
) : ScheduledRedeliveryTemplateStore {
    override fun find(code: String): ScheduledRedeliveryTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRedeliveryTemplate) {
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

    override fun list(): MutableList<ScheduledRedeliveryTemplate> {
        val out = mutableListOf<ScheduledRedeliveryTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRedeliveryTemplateStore(): ScheduledRedeliveryTemplateStore {
    return MemoryScheduledRedeliveryTemplateStore(
        mutableMapOf<String, ScheduledRedeliveryTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRedeliveryTemplateStore(
    var inner: ScheduledRedeliveryTemplateStore,
    var cache: TtlCache<ScheduledRedeliveryTemplate>,
    var hits: Int,
    var misses: Int,
) : ScheduledRedeliveryTemplateStore {
    override fun find(code: String): ScheduledRedeliveryTemplate? {
        val hit = (cache.get(code) as ScheduledRedeliveryTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRedeliveryTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRedeliveryTemplate> = inner.list()
}

fun newCachedScheduledRedeliveryTemplateStore(inner: ScheduledRedeliveryTemplateStore, ttl: Int, clock: Clock): ScheduledRedeliveryTemplateStore {
    return CachedScheduledRedeliveryTemplateStore(
        inner = inner,
        cache = newTtlCache<ScheduledRedeliveryTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled redelivery templates. Empty fields match all. */
data class ScheduledRedeliveryTemplateQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRedeliveryTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRedeliveryTemplate>): MutableList<ScheduledRedeliveryTemplate> {
        val matched = filter(items) { candidate: ScheduledRedeliveryTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRedeliveryTemplates(store: ScheduledRedeliveryTemplateStore, query: ScheduledRedeliveryTemplateQuery): MutableList<ScheduledRedeliveryTemplate> {
    return query.apply(store.list())
}
