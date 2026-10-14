package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default relay templates by code. */
interface DefaultRelayTemplateStore {
    fun find(code: String): DefaultRelayTemplate?
    fun save(item: DefaultRelayTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRelayTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRelayTemplateStore(
    var items: MutableMap<String, DefaultRelayTemplate>,
    var order: MutableList<String>,
) : DefaultRelayTemplateStore {
    override fun find(code: String): DefaultRelayTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRelayTemplate) {
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

    override fun list(): MutableList<DefaultRelayTemplate> {
        val out = mutableListOf<DefaultRelayTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRelayTemplateStore(): DefaultRelayTemplateStore {
    return MemoryDefaultRelayTemplateStore(
        mutableMapOf<String, DefaultRelayTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRelayTemplateStore(
    var inner: DefaultRelayTemplateStore,
    var cache: TtlCache<DefaultRelayTemplate>,
    var hits: Int,
    var misses: Int,
) : DefaultRelayTemplateStore {
    override fun find(code: String): DefaultRelayTemplate? {
        val hit = (cache.get(code) as DefaultRelayTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRelayTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRelayTemplate> = inner.list()
}

fun newCachedDefaultRelayTemplateStore(inner: DefaultRelayTemplateStore, ttl: Int, clock: Clock): DefaultRelayTemplateStore {
    return CachedDefaultRelayTemplateStore(
        inner = inner,
        cache = newTtlCache<DefaultRelayTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default relay templates. Empty fields match all. */
data class DefaultRelayTemplateQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRelayTemplate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRelayTemplate>): MutableList<DefaultRelayTemplate> {
        val matched = filter(items) { candidate: DefaultRelayTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRelayTemplates(store: DefaultRelayTemplateStore, query: DefaultRelayTemplateQuery): MutableList<DefaultRelayTemplate> {
    return query.apply(store.list())
}
