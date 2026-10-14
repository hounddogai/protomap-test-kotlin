package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute templates by code. */
interface DisputeTemplateStore {
    fun find(code: String): DisputeTemplate?
    fun save(item: DisputeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeTemplateStore(
    var items: MutableMap<String, DisputeTemplate>,
    var order: MutableList<String>,
) : DisputeTemplateStore {
    override fun find(code: String): DisputeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeTemplate) {
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

    override fun list(): MutableList<DisputeTemplate> {
        val out = mutableListOf<DisputeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeTemplateStore(): DisputeTemplateStore {
    return MemoryDisputeTemplateStore(mutableMapOf<String, DisputeTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeTemplateStore(
    var inner: DisputeTemplateStore,
    var cache: TtlCache<DisputeTemplate>,
    var hits: Int,
    var misses: Int,
) : DisputeTemplateStore {
    override fun find(code: String): DisputeTemplate? {
        val hit = (cache.get(code) as DisputeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeTemplate> = inner.list()
}

fun newCachedDisputeTemplateStore(inner: DisputeTemplateStore, ttl: Int, clock: Clock): DisputeTemplateStore {
    return CachedDisputeTemplateStore(
        inner = inner,
        cache = newTtlCache<DisputeTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute templates. Empty fields match all. */
data class DisputeTemplateQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeTemplate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<DisputeTemplate>): MutableList<DisputeTemplate> {
        val matched = filter(items) { candidate: DisputeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeTemplates(store: DisputeTemplateStore, query: DisputeTemplateQuery): MutableList<DisputeTemplate> {
    return query.apply(store.list())
}
