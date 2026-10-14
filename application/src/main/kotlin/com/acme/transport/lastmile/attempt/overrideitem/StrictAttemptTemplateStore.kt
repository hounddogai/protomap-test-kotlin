package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict attempt templates by code. */
interface StrictAttemptTemplateStore {
    fun find(code: String): StrictAttemptTemplate?
    fun save(item: StrictAttemptTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAttemptTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAttemptTemplateStore(
    var items: MutableMap<String, StrictAttemptTemplate>,
    var order: MutableList<String>,
) : StrictAttemptTemplateStore {
    override fun find(code: String): StrictAttemptTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAttemptTemplate) {
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

    override fun list(): MutableList<StrictAttemptTemplate> {
        val out = mutableListOf<StrictAttemptTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAttemptTemplateStore(): StrictAttemptTemplateStore {
    return MemoryStrictAttemptTemplateStore(
        mutableMapOf<String, StrictAttemptTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAttemptTemplateStore(
    var inner: StrictAttemptTemplateStore,
    var cache: TtlCache<StrictAttemptTemplate>,
    var hits: Int,
    var misses: Int,
) : StrictAttemptTemplateStore {
    override fun find(code: String): StrictAttemptTemplate? {
        val hit = (cache.get(code) as StrictAttemptTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAttemptTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAttemptTemplate> = inner.list()
}

fun newCachedStrictAttemptTemplateStore(inner: StrictAttemptTemplateStore, ttl: Int, clock: Clock): StrictAttemptTemplateStore {
    return CachedStrictAttemptTemplateStore(
        inner = inner,
        cache = newTtlCache<StrictAttemptTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict attempt templates. Empty fields match all. */
data class StrictAttemptTemplateQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAttemptTemplate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<StrictAttemptTemplate>): MutableList<StrictAttemptTemplate> {
        val matched = filter(items) { candidate: StrictAttemptTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAttemptTemplates(store: StrictAttemptTemplateStore, query: StrictAttemptTemplateQuery): MutableList<StrictAttemptTemplate> {
    return query.apply(store.list())
}
