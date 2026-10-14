package com.acme.transport.network.hub.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending hub templates by code. */
interface PendingHubTemplateStore {
    fun find(code: String): PendingHubTemplate?
    fun save(item: PendingHubTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingHubTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingHubTemplateStore(
    var items: MutableMap<String, PendingHubTemplate>,
    var order: MutableList<String>,
) : PendingHubTemplateStore {
    override fun find(code: String): PendingHubTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingHubTemplate) {
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

    override fun list(): MutableList<PendingHubTemplate> {
        val out = mutableListOf<PendingHubTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingHubTemplateStore(): PendingHubTemplateStore {
    return MemoryPendingHubTemplateStore(mutableMapOf<String, PendingHubTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingHubTemplateStore(
    var inner: PendingHubTemplateStore,
    var cache: TtlCache<PendingHubTemplate>,
    var hits: Int,
    var misses: Int,
) : PendingHubTemplateStore {
    override fun find(code: String): PendingHubTemplate? {
        val hit = (cache.get(code) as PendingHubTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingHubTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingHubTemplate> = inner.list()
}

fun newCachedPendingHubTemplateStore(inner: PendingHubTemplateStore, ttl: Int, clock: Clock): PendingHubTemplateStore {
    return CachedPendingHubTemplateStore(
        inner = inner,
        cache = newTtlCache<PendingHubTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending hub templates. Empty fields match all. */
data class PendingHubTemplateQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingHubTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingHubTemplate>): MutableList<PendingHubTemplate> {
        val matched = filter(items) { candidate: PendingHubTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingHubTemplates(store: PendingHubTemplateStore, query: PendingHubTemplateQuery): MutableList<PendingHubTemplate> {
    return query.apply(store.list())
}
