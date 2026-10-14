package com.acme.transport.network.hub.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub templates by code. */
interface HubTemplateStore {
    fun find(code: String): HubTemplate?
    fun save(item: HubTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubTemplateStore(
    var items: MutableMap<String, HubTemplate>,
    var order: MutableList<String>,
) : HubTemplateStore {
    override fun find(code: String): HubTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubTemplate) {
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

    override fun list(): MutableList<HubTemplate> {
        val out = mutableListOf<HubTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubTemplateStore(): HubTemplateStore {
    return MemoryHubTemplateStore(mutableMapOf<String, HubTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubTemplateStore(
    var inner: HubTemplateStore,
    var cache: TtlCache<HubTemplate>,
    var hits: Int,
    var misses: Int,
) : HubTemplateStore {
    override fun find(code: String): HubTemplate? {
        val hit = (cache.get(code) as HubTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubTemplate> = inner.list()
}

fun newCachedHubTemplateStore(inner: HubTemplateStore, ttl: Int, clock: Clock): HubTemplateStore {
    return CachedHubTemplateStore(
        inner = inner,
        cache = newTtlCache<HubTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub templates. Empty fields match all. */
data class HubTemplateQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubTemplate>): MutableList<HubTemplate> {
        val matched = filter(items) { candidate: HubTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubTemplates(store: HubTemplateStore, query: HubTemplateQuery): MutableList<HubTemplate> {
    return query.apply(store.list())
}
