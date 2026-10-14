package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global transit time templates by code. */
interface GlobalTransitTimeTemplateStore {
    fun find(code: String): GlobalTransitTimeTemplate?
    fun save(item: GlobalTransitTimeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTransitTimeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTransitTimeTemplateStore(
    var items: MutableMap<String, GlobalTransitTimeTemplate>,
    var order: MutableList<String>,
) : GlobalTransitTimeTemplateStore {
    override fun find(code: String): GlobalTransitTimeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTransitTimeTemplate) {
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

    override fun list(): MutableList<GlobalTransitTimeTemplate> {
        val out = mutableListOf<GlobalTransitTimeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTransitTimeTemplateStore(): GlobalTransitTimeTemplateStore {
    return MemoryGlobalTransitTimeTemplateStore(
        mutableMapOf<String, GlobalTransitTimeTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTransitTimeTemplateStore(
    var inner: GlobalTransitTimeTemplateStore,
    var cache: TtlCache<GlobalTransitTimeTemplate>,
    var hits: Int,
    var misses: Int,
) : GlobalTransitTimeTemplateStore {
    override fun find(code: String): GlobalTransitTimeTemplate? {
        val hit = (cache.get(code) as GlobalTransitTimeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTransitTimeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTransitTimeTemplate> = inner.list()
}

fun newCachedGlobalTransitTimeTemplateStore(inner: GlobalTransitTimeTemplateStore, ttl: Int, clock: Clock): GlobalTransitTimeTemplateStore {
    return CachedGlobalTransitTimeTemplateStore(
        inner = inner,
        cache = newTtlCache<GlobalTransitTimeTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global transit time templates. Empty fields match all. */
data class GlobalTransitTimeTemplateQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTransitTimeTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<GlobalTransitTimeTemplate>): MutableList<GlobalTransitTimeTemplate> {
        val matched = filter(items) { candidate: GlobalTransitTimeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTransitTimeTemplates(store: GlobalTransitTimeTemplateStore, query: GlobalTransitTimeTemplateQuery): MutableList<GlobalTransitTimeTemplate> {
    return query.apply(store.list())
}
