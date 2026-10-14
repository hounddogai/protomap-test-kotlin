package com.acme.transport.emissions.idling.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling exceptions by code. */
interface IdlingExceptionStore {
    fun find(code: String): IdlingException?
    fun save(item: IdlingException)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingExceptionStore(
    var items: MutableMap<String, IdlingException>,
    var order: MutableList<String>,
) : IdlingExceptionStore {
    override fun find(code: String): IdlingException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingException) {
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

    override fun list(): MutableList<IdlingException> {
        val out = mutableListOf<IdlingException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingExceptionStore(): IdlingExceptionStore {
    return MemoryIdlingExceptionStore(mutableMapOf<String, IdlingException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingExceptionStore(
    var inner: IdlingExceptionStore,
    var cache: TtlCache<IdlingException>,
    var hits: Int,
    var misses: Int,
) : IdlingExceptionStore {
    override fun find(code: String): IdlingException? {
        val hit = (cache.get(code) as IdlingException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingException> = inner.list()
}

fun newCachedIdlingExceptionStore(inner: IdlingExceptionStore, ttl: Int, clock: Clock): IdlingExceptionStore {
    return CachedIdlingExceptionStore(
        inner = inner,
        cache = newTtlCache<IdlingException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling exceptions. Empty fields match all. */
data class IdlingExceptionQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingException): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingException>): MutableList<IdlingException> {
        val matched = filter(items) { candidate: IdlingException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingExceptions(store: IdlingExceptionStore, query: IdlingExceptionQuery): MutableList<IdlingException> {
    return query.apply(store.list())
}
