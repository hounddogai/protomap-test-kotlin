package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default dispute templates by code. */
interface DefaultDisputeTemplateStore {
    fun find(code: String): DefaultDisputeTemplate?
    fun save(item: DefaultDisputeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDisputeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDisputeTemplateStore(
    var items: MutableMap<String, DefaultDisputeTemplate>,
    var order: MutableList<String>,
) : DefaultDisputeTemplateStore {
    override fun find(code: String): DefaultDisputeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDisputeTemplate) {
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

    override fun list(): MutableList<DefaultDisputeTemplate> {
        val out = mutableListOf<DefaultDisputeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDisputeTemplateStore(): DefaultDisputeTemplateStore {
    return MemoryDefaultDisputeTemplateStore(
        mutableMapOf<String, DefaultDisputeTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDisputeTemplateStore(
    var inner: DefaultDisputeTemplateStore,
    var cache: TtlCache<DefaultDisputeTemplate>,
    var hits: Int,
    var misses: Int,
) : DefaultDisputeTemplateStore {
    override fun find(code: String): DefaultDisputeTemplate? {
        val hit = (cache.get(code) as DefaultDisputeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDisputeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDisputeTemplate> = inner.list()
}

fun newCachedDefaultDisputeTemplateStore(inner: DefaultDisputeTemplateStore, ttl: Int, clock: Clock): DefaultDisputeTemplateStore {
    return CachedDefaultDisputeTemplateStore(
        inner = inner,
        cache = newTtlCache<DefaultDisputeTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dispute templates. Empty fields match all. */
data class DefaultDisputeTemplateQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDisputeTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDisputeTemplate>): MutableList<DefaultDisputeTemplate> {
        val matched = filter(items) { candidate: DefaultDisputeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDisputeTemplates(store: DefaultDisputeTemplateStore, query: DefaultDisputeTemplateQuery): MutableList<DefaultDisputeTemplate> {
    return query.apply(store.list())
}
