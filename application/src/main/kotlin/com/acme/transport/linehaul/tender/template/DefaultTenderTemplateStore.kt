package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default tender templates by code. */
interface DefaultTenderTemplateStore {
    fun find(code: String): DefaultTenderTemplate?
    fun save(item: DefaultTenderTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTenderTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTenderTemplateStore(
    var items: MutableMap<String, DefaultTenderTemplate>,
    var order: MutableList<String>,
) : DefaultTenderTemplateStore {
    override fun find(code: String): DefaultTenderTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTenderTemplate) {
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

    override fun list(): MutableList<DefaultTenderTemplate> {
        val out = mutableListOf<DefaultTenderTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTenderTemplateStore(): DefaultTenderTemplateStore {
    return MemoryDefaultTenderTemplateStore(
        mutableMapOf<String, DefaultTenderTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTenderTemplateStore(
    var inner: DefaultTenderTemplateStore,
    var cache: TtlCache<DefaultTenderTemplate>,
    var hits: Int,
    var misses: Int,
) : DefaultTenderTemplateStore {
    override fun find(code: String): DefaultTenderTemplate? {
        val hit = (cache.get(code) as DefaultTenderTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTenderTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTenderTemplate> = inner.list()
}

fun newCachedDefaultTenderTemplateStore(inner: DefaultTenderTemplateStore, ttl: Int, clock: Clock): DefaultTenderTemplateStore {
    return CachedDefaultTenderTemplateStore(
        inner = inner,
        cache = newTtlCache<DefaultTenderTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tender templates. Empty fields match all. */
data class DefaultTenderTemplateQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTenderTemplate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DefaultTenderTemplate>): MutableList<DefaultTenderTemplate> {
        val matched = filter(items) { candidate: DefaultTenderTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTenderTemplates(store: DefaultTenderTemplateStore, query: DefaultTenderTemplateQuery): MutableList<DefaultTenderTemplate> {
    return query.apply(store.list())
}
