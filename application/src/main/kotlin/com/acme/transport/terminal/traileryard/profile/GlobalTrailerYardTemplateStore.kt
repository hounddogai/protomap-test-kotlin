package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global trailer yard templates by code. */
interface GlobalTrailerYardTemplateStore {
    fun find(code: String): GlobalTrailerYardTemplate?
    fun save(item: GlobalTrailerYardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerYardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerYardTemplateStore(
    var items: MutableMap<String, GlobalTrailerYardTemplate>,
    var order: MutableList<String>,
) : GlobalTrailerYardTemplateStore {
    override fun find(code: String): GlobalTrailerYardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerYardTemplate) {
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

    override fun list(): MutableList<GlobalTrailerYardTemplate> {
        val out = mutableListOf<GlobalTrailerYardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerYardTemplateStore(): GlobalTrailerYardTemplateStore {
    return MemoryGlobalTrailerYardTemplateStore(
        mutableMapOf<String, GlobalTrailerYardTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerYardTemplateStore(
    var inner: GlobalTrailerYardTemplateStore,
    var cache: TtlCache<GlobalTrailerYardTemplate>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerYardTemplateStore {
    override fun find(code: String): GlobalTrailerYardTemplate? {
        val hit = (cache.get(code) as GlobalTrailerYardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerYardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerYardTemplate> = inner.list()
}

fun newCachedGlobalTrailerYardTemplateStore(inner: GlobalTrailerYardTemplateStore, ttl: Int, clock: Clock): GlobalTrailerYardTemplateStore {
    return CachedGlobalTrailerYardTemplateStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerYardTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer yard templates. Empty fields match all. */
data class GlobalTrailerYardTemplateQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerYardTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTrailerYardTemplate>): MutableList<GlobalTrailerYardTemplate> {
        val matched = filter(items) { candidate: GlobalTrailerYardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerYardTemplates(store: GlobalTrailerYardTemplateStore, query: GlobalTrailerYardTemplateQuery): MutableList<GlobalTrailerYardTemplate> {
    return query.apply(store.list())
}
