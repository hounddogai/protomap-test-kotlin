package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict trailer yard templates by code. */
interface StrictTrailerYardTemplateStore {
    fun find(code: String): StrictTrailerYardTemplate?
    fun save(item: StrictTrailerYardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTrailerYardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTrailerYardTemplateStore(
    var items: MutableMap<String, StrictTrailerYardTemplate>,
    var order: MutableList<String>,
) : StrictTrailerYardTemplateStore {
    override fun find(code: String): StrictTrailerYardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTrailerYardTemplate) {
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

    override fun list(): MutableList<StrictTrailerYardTemplate> {
        val out = mutableListOf<StrictTrailerYardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTrailerYardTemplateStore(): StrictTrailerYardTemplateStore {
    return MemoryStrictTrailerYardTemplateStore(
        mutableMapOf<String, StrictTrailerYardTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTrailerYardTemplateStore(
    var inner: StrictTrailerYardTemplateStore,
    var cache: TtlCache<StrictTrailerYardTemplate>,
    var hits: Int,
    var misses: Int,
) : StrictTrailerYardTemplateStore {
    override fun find(code: String): StrictTrailerYardTemplate? {
        val hit = (cache.get(code) as StrictTrailerYardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTrailerYardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTrailerYardTemplate> = inner.list()
}

fun newCachedStrictTrailerYardTemplateStore(inner: StrictTrailerYardTemplateStore, ttl: Int, clock: Clock): StrictTrailerYardTemplateStore {
    return CachedStrictTrailerYardTemplateStore(
        inner = inner,
        cache = newTtlCache<StrictTrailerYardTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict trailer yard templates. Empty fields match all. */
data class StrictTrailerYardTemplateQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTrailerYardTemplate): Boolean {
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

    fun apply(items: MutableList<StrictTrailerYardTemplate>): MutableList<StrictTrailerYardTemplate> {
        val matched = filter(items) { candidate: StrictTrailerYardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTrailerYardTemplates(store: StrictTrailerYardTemplateStore, query: StrictTrailerYardTemplateQuery): MutableList<StrictTrailerYardTemplate> {
    return query.apply(store.list())
}
