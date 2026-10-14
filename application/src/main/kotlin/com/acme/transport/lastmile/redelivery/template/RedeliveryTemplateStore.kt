package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery templates by code. */
interface RedeliveryTemplateStore {
    fun find(code: String): RedeliveryTemplate?
    fun save(item: RedeliveryTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryTemplateStore(
    var items: MutableMap<String, RedeliveryTemplate>,
    var order: MutableList<String>,
) : RedeliveryTemplateStore {
    override fun find(code: String): RedeliveryTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryTemplate) {
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

    override fun list(): MutableList<RedeliveryTemplate> {
        val out = mutableListOf<RedeliveryTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryTemplateStore(): RedeliveryTemplateStore {
    return MemoryRedeliveryTemplateStore(mutableMapOf<String, RedeliveryTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryTemplateStore(
    var inner: RedeliveryTemplateStore,
    var cache: TtlCache<RedeliveryTemplate>,
    var hits: Int,
    var misses: Int,
) : RedeliveryTemplateStore {
    override fun find(code: String): RedeliveryTemplate? {
        val hit = (cache.get(code) as RedeliveryTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryTemplate> = inner.list()
}

fun newCachedRedeliveryTemplateStore(inner: RedeliveryTemplateStore, ttl: Int, clock: Clock): RedeliveryTemplateStore {
    return CachedRedeliveryTemplateStore(
        inner = inner,
        cache = newTtlCache<RedeliveryTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery templates. Empty fields match all. */
data class RedeliveryTemplateQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryTemplate>): MutableList<RedeliveryTemplate> {
        val matched = filter(items) { candidate: RedeliveryTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryTemplates(store: RedeliveryTemplateStore, query: RedeliveryTemplateQuery): MutableList<RedeliveryTemplate> {
    return query.apply(store.list())
}
